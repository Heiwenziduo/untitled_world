package com.github.nahnullscience.cypher_nexus.mechanic.wand.data

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.HelperDataBundle
import com.github.nahnullscience.cypher_nexus.mechanic.wand.IWandLike
import com.github.nahnullscience.cypher_nexus.mechanic.wand.WandDataBundle
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.IWandModule
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.ModuleCategory
import com.github.nahnullscience.cypher_nexus.network.client.ClientboundSyncWandInstance
import com.github.nahnullscience.cypher_nexus.utility.mod.ArrayOfCyphers
import com.github.nahnullscience.cypher_nexus.utility.mod.PosDirePair
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import net.neoforged.neoforge.network.PacketDistributor
import java.util.EnumMap
import kotlin.math.abs

/**
 * hold variable wand data, and handle invoking modules
 * */
class ItemWandInstance(
    val invariable: WandDataInvariable,
    val isClient: Boolean,
    private var aoc: ArrayOfCyphers, // player may edit the wand after the instance has been created
    private val map: ItemWandInstanceMap,
    private val wand: IWandLike
) {
    companion object {
        const val DATA_TOLERANCE_TICK = 2
    }

    val uuid = invariable.uuid
    val manaMax = invariable.chunkF.manaMax
    val manaRegen = invariable.chunkF.manaRegen
    private var _manaCurrent = 0f
    private var _delayCurrent = 0
    private var _rechargeCurrent = 0
    private var _delay0 = 0
    private var _recharge0 = 0
    private var _deck = 0L
    private var _discard = 0L
    private var _lastModifyTime = 0L
    private var _lastInvokeTime = 0L

    private val modules = EnumMap<ModuleCategory, IWandModule>(ModuleCategory::class.java)

    init {
        _manaCurrent = manaMax
        computeModules()
    }

    val manaCurrent get(): Float = _manaCurrent
    val delay get(): Int = _delayCurrent
    val recharge get(): Int = _rechargeCurrent
    val isRecharging get(): Boolean = _rechargeCurrent > 0 && _deck == 0L
    val lastModifyTime get(): Long = _lastModifyTime
    val lastInvokeTime get(): Long = _lastInvokeTime


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    fun tick(entity: Entity) {
        if (_lastModifyTime == entity.level().gameTime) {
            CypherNexus.warn { "$this on [$entity] ticked multiple times" }
            // FIXME unknown bug that client instance occasionally tick twice
            return
        }

        if (_manaCurrent < manaMax) {
            _manaCurrent += manaRegen
            _manaCurrent = _manaCurrent.coerceAtMost(manaMax)
        }

        if (_delayCurrent > 0) _delayCurrent--
        if (_deck == 0L && _rechargeCurrent > 0) _rechargeCurrent--

        _lastModifyTime = entity.level().gameTime // mark the last modify level tick for GC
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    fun isBeginning() = _deck == 0L
    fun canInvoke() = !(_delayCurrent > 0 || (_deck == 0L && _rechargeCurrent > 0))
        .also { println("${side()} invoke check: delay=$_delayCurrent, recharge=$_rechargeCurrent") }

    fun invokeFinish(level: Level) {
        _lastInvokeTime = level.gameTime
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    fun module(category: ModuleCategory) = modules[category]

    private fun computeModules() {
        modules.clear()
        aoc.modulesSequenceReverse().forEach { moduleCypher ->
            println("compute module: $moduleCypher")
            moduleCypher.apply(modules)
        }
        CypherNexus.debugWand { "$this computed modules, current module: $modules" }
    }

    fun leftClickModule() {

    }

    fun rightClickModule() {

    }

    fun middleClickModule() {

    }

    /** should call on both sides */
    fun recoilModule(invoker: Entity, recoil: Double, invokePosDire: PosDirePair) {
        // TODO recoil module
        // for now, push invoker for ease
//        println("do some recoil: $recoil   ${side()}")

        // since it is the client side that is Player position authoritative
        // this logic should run on both side, client for smooth movement, server for verification
        val dire = if (invokePosDire.direction != Vec3.ZERO) invokePosDire.direction
        else invoker.eyePosition.vectorTo(invokePosDire.position)
        val recoil0 = recoil / 20
        invoker.push(dire.normalize().scale(recoil0).reverse())
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    fun manaRegenPercent(partialTick: Float) = (manaCurrent + partialTick * manaRegen) / manaMax
    fun delayPercent(partialTick: Float) = (_delay0.toFloat() - _delayCurrent + partialTick) / _delay0
    fun rechargePercent(partialTick: Float) = (_recharge0.toFloat() - _rechargeCurrent + partialTick) / _recharge0


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /** when cypher-list is edited */
    fun updateWandStatsServer(bundle: WandDataBundle) {
        if (isClient) CypherNexus.LOGGER.error("server method calls on client side: updateWandStatsServer")
        _deck = 0
        _discard = 0
        _rechargeCurrent = 0
        updateAoc(bundle.highPayload.aoc)
    }

    fun updateAoc(aoc: ArrayOfCyphers) {
        // can be called on both sides
        CypherNexus.debugWand { "$this just updated cyphers: $aoc" }
        this.aoc = aoc
        computeModules()
    }

    /**
     * sync mana / delay / recharge state after invoking
     * */
    fun syncInvokingDataClient(mana: Float, delay: Int, recharge: Int, deck: Long) {
        if (!isClient) CypherNexus.LOGGER.error("client method calls on server side: syncDataClient")

        // give a 2 ticks tolerance to prevent bar-flash
        var badNetwork = false
        if (abs(_manaCurrent - mana) > DATA_TOLERANCE_TICK * manaRegen) _manaCurrent = mana .also { badNetwork = true }
        if (abs(_delayCurrent - delay) > DATA_TOLERANCE_TICK) _delayCurrent = delay .also { badNetwork = true }
        if (abs(_rechargeCurrent - recharge) > DATA_TOLERANCE_TICK) _rechargeCurrent = recharge .also { badNetwork = true }

        _deck = deck
        if (badNetwork) {

        }
    }

    fun toHelperDataBundle() = HelperDataBundle(
        draw = invariable.chunkI.draw,
        delay = invariable.chunkI.castDelay,
        recharge = if (isBeginning()) invariable.chunkI.rechargeTime else _rechargeCurrent,
        manaCurrent = _manaCurrent,
        deck = _deck,
        discard = _discard
    )

    fun updateFromHelperData(bundle: HelperDataBundle) {
        _manaCurrent        =   bundle.manaCurrent
        _delayCurrent       =   bundle.delay
        _rechargeCurrent    =   bundle.recharge
        _delay0             =   bundle.delay
        _recharge0          =   bundle.recharge
        _deck               =   bundle.deck
        _discard            =   bundle.discard
    }

    fun sendSyncStatePacket(player: ServerPlayer) {
        if (isClient) {
            CypherNexus.LOGGER.error("server method calls on client side: sendSyncStatePacket")
            return
        }
        PacketDistributor.sendToPlayer(
            player,
            ClientboundSyncWandInstance(
                uuid,
                _manaCurrent,
                _delayCurrent,
                _rechargeCurrent,
                _deck
            )
        )
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    fun side() = if (isClient) "client" else "server"
    override fun toString() = "${side()} wand-instance: [$uuid]"
}