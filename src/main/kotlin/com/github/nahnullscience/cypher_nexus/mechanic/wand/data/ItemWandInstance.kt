package com.github.nahnullscience.cypher_nexus.mechanic.wand.data

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.HelperDataBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingState
import com.github.nahnullscience.cypher_nexus.mechanic.wand.WandDataBundle
import com.github.nahnullscience.cypher_nexus.utility.mod.ArrayOfCyphers
import com.github.nahnullscience.cypher_nexus.utility.mod.PosDirePair
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3

/**
 * hold variable wand data, and handle invoking modules
 * */
class ItemWandInstance(
    val invariable: WandDataInvariable,
    val isClient: Boolean,
    // TODO update client aoc when wand edit (item-component is auto synced, but the reference inside instance is not)
    private var aoc: ArrayOfCyphers, // player may edit the wand after the instance has been created
    private val map: ItemWandInstanceMap
) {
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

    init {
        _manaCurrent = manaMax
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
            CypherNexus.warn { "wand [${invariable.uuid}] on [$entity] ticked multiple times" }
            return
        }
        _manaCurrent += manaRegen
        _manaCurrent = _manaCurrent.coerceAtMost(manaMax)

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
    fun rightClickModule() {

    }

    fun leftClickModule() {

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
        aoc = bundle.highPayload.cypherArray
    }

    fun syncDataClient(mana: Float, delay: Int, recharge: Int, deck: Long) {
        if (!isClient) CypherNexus.LOGGER.error("client method calls on server side: syncDataClient")
        _manaCurrent     = mana
        _delayCurrent    = delay
        _rechargeCurrent = recharge
        _delay0          = delay
        _recharge0       = recharge
        _deck            = deck
    }

    fun toHelperDataBundle() = HelperDataBundle(
        draw = invariable.chunkI.draw,
        delay = invariable.chunkI.castDelay,
        recharge = if (isBeginning()) invariable.chunkI.rechargeTime else _rechargeCurrent,
        manaCurrent = _manaCurrent,
        deck = _deck,
        discard = _discard
    )

    fun updateHelperData(bundle: HelperDataBundle) {
        _manaCurrent        =   bundle.manaCurrent
        _delayCurrent       =   bundle.delay
        _rechargeCurrent    =   bundle.recharge
        _delay0             =   bundle.delay
        _recharge0          =   bundle.recharge
        _deck               =   bundle.deck
        _discard            =   bundle.discard
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    fun side() = if (isClient) "client" else "server"
    override fun toString() = "wand-instance: ${invariable.uuid}"
}