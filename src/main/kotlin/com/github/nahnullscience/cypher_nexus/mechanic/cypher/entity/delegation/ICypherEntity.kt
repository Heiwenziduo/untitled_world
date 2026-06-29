package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation

import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.DiscardReason
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookContainer
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HooksSharedData
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.TriggerType
import com.github.nahnullscience.cypher_nexus.utility.i.IFlagExtension
import com.github.nahnullscience.cypher_nexus.utility.mod.MapOfCypherCounts
import com.github.nahnullscience.cypher_nexus.utility.mod.PosDirePair
import net.minecraft.core.Holder
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.TraceableEntity
import net.minecraft.world.entity.animal.Animal
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3

/**
 * define data pieces that all cypher-entity would require.
 * those pieces then could be delivered through Delegation
 * */
interface ICypherEntity : TraceableEntity, IFlagExtension {
    companion object {
        const val CLIP_MARGIN = 0.2f
        const val CAPTURE_SIZE = 8.0
        const val CAPTURE_SIZE_SQR = CAPTURE_SIZE * CAPTURE_SIZE
        const val LOW_SPEED_THRESHOLD = 0.02
        const val LOW_SPEED_THRESHOLD_SQR = LOW_SPEED_THRESHOLD * LOW_SPEED_THRESHOLD
        const val HIT_BB_INFLATION = 0.25
    }

    val cypherHolder: Holder<out AbstractProjectileCypher<*>>
    val cypher get() = cypherHolder.value()

//    val ccMap: MapOfCypherCounts?
    val attributeMap: Map<CypherAttribute, Double>

    val hooks: HookContainer?
    val hooksSharedData: HooksSharedData<*>

    val trigger: TriggerType
    val payload: ShotStateChunk?

    /**
     * initialize from [MapOfCypherCounts]
     * */
    fun initCypher(map: MapOfCypherCounts?)
    /**
     * init from [ShotStateChunk]
     * */
    fun initCypher(state: ShotStateChunk)
    /**
     *
     * */
    fun <E> initEntity(cy: E) where E : Entity, E : ICypherEntity
    /**
     *
     * */
    fun initDirection(direction: Vec3? = null)
    /**
     *
     * */
    fun initDirection(pair: PosDirePair)


    // quick access for common attributes
    var existing: Int
    val speed: Double get() = getAttrOrProjDefault(CypherAttributes.SPEED)
    val bounce: Int get() = getAttrOrProjDefault(CypherAttributes.BOUNCE).toInt()
    val canBounce: Boolean
    /**
     * store bounce points triggered in one tick
     * */
    val bouncePoints: List<Vec3>
    val bouncedThisTick: Boolean get() = bouncePoints.isNotEmpty()
    val gravity: Float get() = getAttrOrProjDefault(CypherAttributes.GRAVITY_FACTOR).toFloat()
    val speedFactor: Float get() = 1f - getAttrOrProjDefault(CypherAttributes.FRICTION_FACTOR).toFloat()
    val rotationSpeed: Float
    val effectRadius: Double get() = getAttrOrProjDefault(CypherAttributes.EFFECT_RADIUS)

    // attributes access functions
    fun getAttribute(attr: CypherAttribute): Double? = attributeMap[attr]
    fun getAttribute(holer: Holder<CypherAttribute>): Double? = getAttribute(holer.value())
    /** get value through entity-specific map > cypher default > attribute default */
    fun getAttrOrProjDefault(attr: CypherAttribute): Double = attributeMap[attr] ?: cypher.getAttrBaseOrDefault(attr)
    /** get value through entity-specific map > cypher default > attribute default */
    fun getAttrOrProjDefault(holer: Holder<CypherAttribute>): Double = getAttrOrProjDefault(holer.value())

    //
    fun needCaptureSurrounding() = false
    fun underwaterSpeedFactor() = 0.8f
    fun inWallSpeedFactor() = 0.5f
    fun bounceSpeedPenalty() = 0.9

    fun trigger(type: TriggerType)

    fun discardCypher(reason: DiscardReason)

    // hooks // TODO extensive refactor
    fun beforeDiscardBoth(reason: DiscardReason) {}
    fun hitBoth(result: HitResult) {}
    fun firstTickBoth() {}
    fun tickBehaviorBoth() {}
    fun tickFinalizeMovementBoth() {}
    fun bounceBoth(bouncePoint: Vec3) {}
    fun captureSurroundingBoth(captured: Entity) {}
    fun lowSpeedBoth(count: Int) {
        if (count < 40) return
        if (speed > LOW_SPEED_THRESHOLD) {
            discardCypher(DiscardReason.LOW_SPEED)
        }
    }

    /**
     * should call inside Entity#tick, this handles all cypher-related logic
     * */
    fun doTick()
    /**
     * use as general entity selector through [net.minecraft.world.level.Level.getEntities]
     * */
    fun canHitTarget(target: Entity): Boolean {
        if (!target.canBeHitByProjectile()) {
            return false // vanilla logic, for item-entities
        }
        if (owner == target && notHaveFlag(CypherFlags.HURT_OWNER)) return false
        return true
    }
    /**
     * when the entity "hit" something,
     * both [net.minecraft.world.phys.EntityHitResult] and [net.minecraft.world.phys.BlockHitResult]
     * will be passed into this method. this method is called on both sides
     * */
    fun whenHit(result: HitResult)
    /**
     *
     * */
    fun canHomeTarget(target: Entity): Boolean {
        return target !is Animal
                && target !is ItemEntity
                && !target.isInvisible
                && target.isAlive
                && target != owner
    }
    /**
     *
     * */
    fun whileHomeTarget(target: Entity) {}

    override fun getOwner(): Entity?
    fun setOwner(owner: Entity?)
}