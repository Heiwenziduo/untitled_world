package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components

import com.github.nahnullscience.cypher_nexus.init.mod.CypherHooks
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.DiscardReason
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity.Companion.CAPTURE_SIZE
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity.Companion.LOW_SPEED_THRESHOLD_SQR
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.steerer.AbstractCypherSteerer
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookContainer
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HooksSharedData
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk
import com.github.nahnullscience.cypher_nexus.utility.i.IFlagExtension
import com.github.nahnullscience.cypher_nexus.utility.mod.MapOfCypherCounts
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.TraceableEntity
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3

interface ICypherEntityLogicContext : TraceableEntity, IFlagExtension {

    /**
     * [MapOfCypherCounts] serves as the token of [ShotStateChunk],
     * this field initialized in server and will be shipped to client to sync shot-data
     *
     * this field directly forwards to the backing [ShotStateChunk] and should be treated as `immutable`
     * */
    val ccMap: MapOfCypherCounts?

    /** this field directly forwards to the backing [ShotStateChunk] and should be treated as `immutable` */
    val hooks: HookContainer?
    val hooksSharedData: HooksSharedData<*>
    val steerer: AbstractCypherSteerer

    /** tint from dyes */
    val hue: Int?
    /** 0f~1f float representation of [hue], in order of r0 g1 b2 a3 */
    val hueFloatArray: FloatArray?

    override fun getOwner(): Entity?
    fun setOwner(owner: Entity?)

    /**
     *
     * */
    fun getDamageSource(): DamageSource

    fun getExisting(): Int
    fun getBounce(): Int
    fun getGravityFactor(): Double
    fun getSpeedFactor(): Double
    fun getEffectRadius(): Float
    /**
     * used as a factor inside `Entity.rotateTowardSpeed`,
     * the higher the faster the entity will rotate, to face the direction the deltaMovement is pointed at
     * */
    fun getUnderwaterSpeedFactor() = 0.8
    fun getInWallSpeedFactor() = 0.5
    fun getBounceSpeedPenalty() = 0.95
    fun getRotationSpeed(): Float = 0.25f



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
        if (ce.tickCount == 1 || (ce.tickCount - 2) and 3 == 3) { // trigger on tick 1, and then every 4 ticks
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
                        ce.boundingBox.inflate(CAPTURE_SIZE)
                    ) { entity -> ce.canHitTarget(entity) && entity !is ICypherEntity }

                    for (entity in entities) {
                        ce.forEntityCaptured(ce, entity)
                    }
                }
            }
        }
    }
    /**
     * call on both sides, override friendly
     * @see CypherHooks.BEFORE_DISCARD
     * */
    fun <CE> beforeDiscard(ce: CE, reason: DiscardReason) where CE : Entity, CE : ICypherEntity {
        hooks?.playHooks(CypherHooks.BEFORE_DISCARD) { index, hook, count ->
            hook.beforeDiscard(index, count, ce.level(), ce, reason)
        }
    }
    /**
     * call on both sides, override friendly
     * @see CypherHooks.HIT_ENTITY
     * */
    fun <CE> onHit(ce: CE, result: HitResult) where CE : Entity, CE : ICypherEntity {
        hooks?.playHooks(CypherHooks.HIT_ENTITY) { index, hook, count ->
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
    fun <CE> onBounce(ce: CE, bouncePoint: Vec3, bounceCount: Int) where CE : Entity, CE : ICypherEntity {
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
    /**
     * call on both sides, override friendly
     * @see
     * */
    fun <CE> onLowSpeed(ce: CE, ticks: Int, initialSpeedSqr: Double) where CE : Entity, CE : ICypherEntity {
        if (ticks < 7) return

        // this means the projectile is decelerated to low speed
        if (initialSpeedSqr > LOW_SPEED_THRESHOLD_SQR && noFlag(CypherFlags.MOTION_FOLLOWS_OWNER)) {
            ce.discardCypher(DiscardReason.LOW_SPEED)
        }
    }

//    /** only on Server */
//    fun onDealDamage(damage: Double)
}