package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components

import com.github.nahnullscience.cypher_nexus.init.mod.CypherHooks
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.DiscardReason
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity.Companion.GENERIC_CAPTURE_RADIUS
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.steerer.AbstractCypherSteerer
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookContainer
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HooksSharedData
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk
import com.github.nahnullscience.cypher_nexus.utility.i.IFlagExtension
import com.github.nahnullscience.cypher_nexus.utility.mod.MapOfCypherCounts
import net.minecraft.core.Direction
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.TraceableEntity
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3

interface ICypherEntityLogicContext : TraceableEntity,
    IFlagExtension, ICypherEntityAttributeAccessor {

    /**
     * [MapOfCypherCounts] serves as the token of [ShotStateChunk],
     * this field initialized in server and will be shipped to client to sync shot-data
     *
     * this field directly forwards to the backing [ShotStateChunk] and should be treated as `immutable`
     * */
    val ccMap: MapOfCypherCounts?

    /** this field directly forwards to the backing [ShotStateChunk] and should be treated as `immutable` */
    val hooks: HookContainer?
    val hooksSharedData: HooksSharedData
    val steerer: AbstractCypherSteerer

    /***/
    val dyed: Boolean
    /** tint from dyes */
    val hue: Int
    /** 0f~1f float representation of [hue], in order of r0 g1 b2 a3 */
    val hueFloatArray: FloatArray

    val explosion: ExplosionSettings<*>?

    override fun getOwner(): Entity?
    fun setOwner(owner: Entity?)

    fun <CE> canHurtOwner(ce: CE): Boolean where CE : Entity, CE : ICypherEntity =
        ce.hasFlag(CypherFlags.HURT_OWNER) && ce.tickCount > 1

    /**
     * used as a factor inside `Entity.rotateTowardSpeed`,
     * the higher the faster the entity will rotate, to face the direction the deltaMovement is pointed at
     * */
    fun getUnderwaterSpeedFactor() = 0.8
    fun getInWallSpeedFactor() = 0.5
    fun getBounceSpeedPenalty() = 0.95
    fun getRotationSpeed(): Float = 0.25f

    /**
     *
     * */
    fun getDamageSource(): DamageSource

    /**
     *
     * */
    fun getExplosionDamageSource(): DamageSource

    /**
     * use as general entity selector through [net.minecraft.world.level.Level.getEntities]
     * */
    fun canHitTarget(target: Entity): Boolean
    /**
     * when the entity "hit" something,
     * both [EntityHitResult] and [BlockHitResult]
     * will be passed into this method.
     *
     * this method is called on both sides
     * */
    fun whenHit(result: HitResult, direction: Direction) = Unit
    fun whenHitEntity(result: EntityHitResult, direction: Direction) = Unit
    fun whenHitBlock(result: BlockHitResult, direction: Direction) = Unit

    /**
     *
     * */
    fun canHomeTarget(target: Entity): Boolean

    /**
     *
     * */
    fun whileHomeTarget(target: Entity) = Unit



    // hooks // TODO extensive refactor

    /**
     *
     * */
    fun needCaptureSurrounding(): Boolean = false
    /**
     * call on both sides, override friendly
     * @see CypherHooks.ENTITY_CAPTURE
     * */
    fun <CE> captureSurroundings(ce: CE) where CE : Entity, CE : ICypherEntity {
        hooks?.get(CypherHooks.ENTITY_CAPTURE)?.let {
            val level = ce.level()
            var need = ce.needCaptureSurrounding()
            if (!need) run {
                hooks?.cumulateHooks(CypherHooks.ENTITY_CAPTURE, false) { _, hook, _, _ ->
                    need = hook.needCapture(level, ce)
                    if (need) return@run
                    false
                }
            }

            if (need) {
                val entities = level.getEntities(
                    ce,
                    ce.boundingBox.inflate(GENERIC_CAPTURE_RADIUS)
                ) { entity -> ce.canHitTarget(entity) && entity !is ICypherEntity }

                for (entity in entities) {
                    ce.forEntityCaptured(ce, entity)
                }
            }
        }
    }
    /**
     * call on both sides, override friendly
     * @see CypherHooks.BEFORE_DISCARD_SERVER
     * */
    fun <CE> beforeDiscardServer(ce: CE, reason: DiscardReason) where CE : Entity, CE : ICypherEntity {
        hooks?.playHooks(CypherHooks.BEFORE_DISCARD_SERVER) { index, hook, count ->
            hook.beforeDiscardServer(index, count, ce.level(), ce, reason)
        }
    }
    /**
     * call on both sides, override friendly
     * @see CypherHooks.GENERAL_HIT
     * */
    fun <CE> onHit(ce: CE, result: HitResult) where CE : Entity, CE : ICypherEntity {
        hooks?.playHooks(CypherHooks.GENERAL_HIT) { index, hook, count ->
            hook.onHit(index, count, ce.level(), ce, result)
        }
    }
    /**
     * call on both sides, override friendly
     * @see CypherHooks.FIRST_TICK
     * */
    fun <CE> onFirstTick(ce: CE) where CE : Entity, CE : ICypherEntity {
        hooks?.playHooks(CypherHooks.FIRST_TICK) { index, hook, count ->
            hook.onFirstTick(index, count, ce.level(), ce)
        }
    }
    /**
     * call on both sides, override friendly
     * @see CypherHooks.TICK_BEHAVIOR
     * */
    fun <CE> onTick(ce: CE) where CE : Entity, CE : ICypherEntity {
        hooks?.playHooks(CypherHooks.TICK_BEHAVIOR) { index, hook, count ->
            hook.onTick(index, count, ce.level(), ce)
        }
    }
    /**
     * call on both sides, override friendly
     * @see CypherHooks.TICK_MOVEMENT_FINALIZE
     * */
    fun <CE> finalizeTickMovement(ce: CE) where CE : Entity, CE : ICypherEntity {
        hooks?.playHooks(CypherHooks.TICK_MOVEMENT_FINALIZE) { index, hook, count ->
            hook.finalizeTickMovement(index, count, ce.level(), ce)
        }
    }
    /**
     * call on both sides, override friendly
     * @see CypherHooks.ON_BOUNCE
     * */
    fun <CE> onBounce(ce: CE, bouncePoint: Vec3, bounceSurface: Direction, bounceCount: Int) where CE : Entity, CE : ICypherEntity {
        hooks?.playHooks(CypherHooks.ON_BOUNCE) { index, hook, count ->
            hook.onBounce(index, count, ce.level(), ce, bounceCount, bouncePoint)
        }
    }
    /**
     * call on both sides, override friendly
     * @see CypherHooks.ENTITY_CAPTURE
     * */
    fun <CE> forEntityCaptured(ce: CE, captured: Entity) where CE : Entity, CE : ICypherEntity {
        hooks?.playHooks(CypherHooks.ENTITY_CAPTURE) { index, hook, count ->
            hook.forEntityCaptured(index, count, ce.level(), ce, captured)
        }
    }

    companion object {
        fun <CE> CE.canNotHurtOwner(): Boolean where CE : Entity, CE : ICypherEntity = !canHurtOwner(this)
    }
}