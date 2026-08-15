package com.github.nahnullscience.cypher_nexus.mechanic.wand.data

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.HelperDataBundle
import com.github.nahnullscience.cypher_nexus.mechanic.wand.IItemWand
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.MapOfModules
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.ModuleSlot.Companion.DEFAULT_INVOKING
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.ModuleSlot.Companion.DEFAULT_RECOIL
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.ModuleSlot.Companion.DEFAULT_SECONDARY
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.WandModuleType
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.AbstractFunctionalModule
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.AbstractWandModule
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.ITypeUniqueModule
import com.github.nahnullscience.cypher_nexus.network.client.ClientboundSyncWandInstance
import com.github.nahnullscience.cypher_nexus.utility.linear_space.CoordinateDefinition
import com.github.nahnullscience.cypher_nexus.utility.mod.ArrayOfCyphers
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.neoforged.neoforge.network.PacketDistributor
import java.util.function.Supplier
import kotlin.math.abs

/**
 * hold variable wand data, and handle invoking modules
 * */
class ItemWandInstance(
    val wandData: ItemWandDataInvariable,
    val wand: IItemWand,
    val isClient: Boolean,
    private var aoc: ArrayOfCyphers, // player may edit the wand after the instance has been created
    private val map: ItemWandInstanceMap,
) {
    companion object {
        private const val SYNC_TOLERANCE_TICK = 2
    }

    val uuid = wandData.uuid

    val manaMax get(): Float = wandData.chunkF.manaMax
    val manaRegen get(): Float = wandData.chunkF.manaRegen
    val manaCurrent get(): Float = _manaCurrent
    val delay get(): Int = _delayCurrent
    val recharge get(): Int = _rechargeCurrent
    val isRecharging get(): Boolean = _rechargeCurrent > 0 && _deck == 0L
    val lastModifyTime get(): Long = _lastModifyTime
    val lastInvokeTime get(): Long = _lastInvokeTime

    private var _manaCurrent = 0f
    private var _delayCurrent = 0
    private var _rechargeCurrent = 0
    private var _delay0 = 0
    private var _recharge0 = 0
    private var _deck = 0L
    private var _discard = 0L
    private var _lastModifyTime = 0L
    private var _lastInvokeTime = 0L

    private val modules = MapOfModules(this)

    init {
        _manaCurrent = manaMax
        computeModules()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    fun tick(entity: Entity) {
        if (_lastModifyTime == entity.level().gameTime) {
            CypherNexus.warn { "$this on [${entity.javaClass.name}] ticked multiple times" }
            // FIXME unknown bug that client instance occasionally tick twice
//            Thread.dumpStack()
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
    fun canInvoke() = !(_delayCurrent > 0 || (_deck == 0L && _rechargeCurrent > 0)) && aoc.invokableSize > 0
//        .also { println("${side()} invoke check: delay=$_delayCurrent, recharge=$_rechargeCurrent") }

    fun invokeFinish(level: Level) {
        _lastInvokeTime = level.gameTime
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    fun <T> getModule(type: WandModuleType<T>): T? where T : AbstractWandModule, T : ITypeUniqueModule = modules[type]
    fun <T> getModule(holder: Supplier<out WandModuleType<T>>): T? where T : AbstractWandModule, T : ITypeUniqueModule = modules[holder]

    private fun computeModules() {
        modules.clear()
        aoc.modulesSequenceReverse().forEach { moduleCypher ->
            moduleCypher.moduleSlots.forEach { slot -> modules.registerSlot(slot) }
        }
        modules.registerSlot(DEFAULT_INVOKING)
        modules.registerSlot(DEFAULT_RECOIL)
        modules.registerSlot(DEFAULT_SECONDARY)
        modules.finalizeInit()
    }

    /**
     *
     * */
    fun <T> functionModule(
        type: WandModuleType<T>,
        invoker: LivingEntity,
        wand: ItemStack? = null,
        invokerCoordinate: CoordinateDefinition? = null,
        indirectTarget: Entity? = null,
        performingTicks: Int = -1,
        power: Double = Double.NaN,
    ): Boolean where T : AbstractFunctionalModule {
        return getModule(type)?.run { execute(invoker, wand, invokerCoordinate, indirectTarget, performingTicks, power) } ?: false
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    fun manaRegenPercent(partialTick: Float) = (manaCurrent + partialTick * manaRegen) / manaMax
    fun delayPercent(partialTick: Float) = (_delay0.toFloat() - _delayCurrent + partialTick) / _delay0
    fun rechargePercent(partialTick: Float) = (_recharge0.toFloat() - _rechargeCurrent + partialTick) / _recharge0


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /** when cypher-list is edited */
    fun updateWandStatsServerOnly(wandData: ItemWandDataInvariable, aoc: ArrayOfCyphers) {
        if (isClient) CypherNexus.LOGGER.error("server method calls on client side: updateWandStatsFromServer")
        _deck = 0
        _discard = 0
        _rechargeCurrent = 0
        updateAoc(aoc)
    }

    fun updateAoc(aoc: ArrayOfCyphers) {
        // can be called on both sides
        CypherNexus.debugWand { "$this just updated cyphers: $aoc" }
        this.aoc = aoc
        computeModules()
    }

    /**
     * when receive package from [sendSyncStatePacketServerOnly],
     * sync mana / delay / recharge state after invoking
     * */
    fun syncInvokingDataClientOnly(mana: Float, delay: Int, recharge: Int, deck: Long) {
        if (!isClient) CypherNexus.LOGGER.error("client method calls on server side: syncDataClient")

        // give a 2 ticks tolerance to prevent bar-flash
        var badNetwork = false
        if (abs(_manaCurrent - mana) > SYNC_TOLERANCE_TICK * manaRegen) _manaCurrent = mana .also { badNetwork = true }
        if (abs(_delayCurrent - delay) > SYNC_TOLERANCE_TICK) _delayCurrent = delay .also { badNetwork = true }
        if (abs(_rechargeCurrent - recharge) > SYNC_TOLERANCE_TICK) _rechargeCurrent = recharge .also { badNetwork = true }

        _deck = deck
        if (badNetwork) {

        }
    }

    fun toHelperDataBundle() = HelperDataBundle(
        manaCurrent = _manaCurrent,
        draw = wandData.chunkI.draw,
        delay = wandData.chunkI.castDelay,
        recharge = if (isBeginning()) wandData.chunkI.rechargeTime else _rechargeCurrent,
        deck = _deck,
        discard = _discard
    )

    fun updateFromHelperData(bundle: HelperDataBundle) {
        bundle.coerceData()
        _manaCurrent        =   bundle.manaCurrent
        _delayCurrent       =   bundle.delay
        _rechargeCurrent    =   bundle.recharge
        _delay0             =   bundle.delay
        _recharge0          =   bundle.recharge
        _deck               =   bundle.deck
        _discard            =   bundle.discard
    }

    fun sendSyncStatePacketServerOnly(player: ServerPlayer) {
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

    /**
     * called when instance off-link from the map.
     * */
    fun discard() {
        modules.clear()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    fun side() = if (isClient) "client" else "server"
    override fun toString() = "${side()} wand-instance: [$uuid]"
}