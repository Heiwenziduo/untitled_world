package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.DiscardReason
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity.Companion.HIT_BB_INFLATION
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity.Companion.LOW_SPEED_THRESHOLD_SQR
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity.Companion.exertDamage
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntityAttributeAccessor.Companion.getBounce
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntityAttributeAccessor.Companion.getExisting
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntityAttributeAccessor.Companion.getGravityFactor
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntityAttributeAccessor.Companion.getSpeedFactor
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileNode
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.TriggerType
import com.github.nahnullscience.cypher_nexus.utility.*
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.profiling.Profiler
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.ClipContext.Block
import net.minecraft.world.level.ClipContext.Fluid
import net.minecraft.world.level.gameevent.GameEvent
import net.minecraft.world.level.gameevent.GameEvent.Context
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.HitResult.Type
import net.minecraft.world.phys.Vec3
import kotlin.math.pow

open class CEPhysicsBasics <CE> : ICEPhysics<CE> where CE : Entity, CE : ICypherEntity {
    protected lateinit var cyEntity: CE
    protected val level get() = cyEntity.level()
    protected val random get() = cyEntity.random


    override var triggerType = TriggerType.NONE
    override var payload: ShotStateChunk? = null

    protected var bounceCount = 0
    override val canBounce: Boolean get() = bounceCount < cyEntity.getBounce()
    override val bouncePoints = ArrayList<Vec3>()
    override val bouncedThisTick: Boolean get() = bouncePoints.isNotEmpty()

    protected var capturedInitialSpeedSqr: Double = 0.0
    protected var lowSpeedTickCount = 0

    protected val collideWithBlocks: Boolean get() = cyEntity.noFlagsNone(CypherFlags.IGNORE_BLOCK, CypherFlags.PENETRATE_WORLD)
    protected val collideWithEntities: Boolean get() = cyEntity.noFlagsNone(CypherFlags.PENETRATE_WORLD)

    override fun initCypher(cypher: AbstractProjectileCypher<*>, shotState: ShotStateChunk?, node: ProjectileNode?) {
        if (node != null) {
            triggerType = node.trigger
            payload = node.payload
        }
    }

    override fun initEntity(ce: CE) = let { cyEntity = ce }



    protected open fun applyGravity() {
        if (cyEntity.getGravityFactor() != 0.0)
            cyEntity.deltaMovement = cyEntity.deltaMovement.add(0.0, -cyEntity.getGravityFactor(), 0.0)
    }

    protected open fun applyFriction() {
        val f: Double =
            if (cyEntity.isInWater) cyEntity.getUnderwaterSpeedFactor() * cyEntity.getSpeedFactor()
            else cyEntity.getSpeedFactor()
        if (f != 1.0) cyEntity.deltaMovement *= f
    }

    override fun trigger(type: TriggerType, releaseTo: PosDirePair) {
        payload?.release(
            level,
            cyEntity.perspectiveCoordinate(),
            releaseTo,
            cyEntity,
            cyEntity.owner,
            null
        )
    }
    protected fun trigger(type: TriggerType, releasePoint: Vec3) {
        if (level.isClientSide) return
        if (triggerType != TriggerType.NONE && type == triggerType)
            when (type) {
                TriggerType.COLLISION ->
                    cyEntity.trigger(type, PosDirePair(releasePoint, cyEntity.deltaMovement.reverse()))
                else ->
                    cyEntity.trigger(type, PosDirePair(releasePoint, cyEntity.deltaMovement))
            }
    }


    override fun discardCypher(reason: DiscardReason) {
        if (level.isClientSide) return
        trigger(TriggerType.DEATH, cyEntity.position())
        when(reason){
            DiscardReason.ERASE -> {}
            else -> {
                cyEntity.beforeDiscard(cyEntity, reason)
                cyEntity.steerer.discard(cyEntity, reason)
            }
        }
        level.broadcastEntityEvent(cyEntity, 3)
        cyEntity.discard()
    }

    override fun doTick() {
        Profiler.get().push { "cypherEntityTick" }

        if (cyEntity.tickCount == 1) {
            cyEntity.onFirstTick(cyEntity)
            cyEntity.steerer.init(cyEntity)
        }
        if (triggerType.timer == cyEntity.tickCount) trigger(triggerType, cyEntity.position())

        cyEntity.captureSurroundings(cyEntity)

        if (cyEntity.tickCount == 3) {
            capturedInitialSpeedSqr = cyEntity.deltaMovement.lengthSqr()
        }

//        cyEntity.hooksSharedData.tick(cyEntity)
//        if (moveAsProjectile)
        projectileTick()

        if (cyEntity.getExisting() <= cyEntity.tickCount && cyEntity.getExisting() != 0) {
            // here's a trick, if player make existing-time exactly equal to 0, projectile will last till the game quit
            discardCypher(DiscardReason.EXPIRE)
        }

        Profiler.get().pop()
    }

    /**
     * handle movement
     * */
    protected open fun projectileTick() {
        /*
         * deltaMovement: the movement for the "next tick", client smooth animation relay on this
         * an AABB check is used everyTick every vanilla projectile, sounds outrageous, but is ok in performance
         * */
        cyEntity.onTick(cyEntity)
        cyEntity.steerer.tick(cyEntity)

        cyEntity.rotateTowardSpeed(cyEntity.getRotationSpeed())
        cyEntity.finalizeTickMovement(cyEntity)
        cyEntity.steerer.tickSpeedOverride(cyEntity)

        if (cyEntity.deltaMovement.lengthSqr() <= LOW_SPEED_THRESHOLD_SQR)
            cyEntity.onLowSpeed(cyEntity, lowSpeedTickCount++, capturedInitialSpeedSqr)
        else lowSpeedTickCount = 0

        loopHitAndBounce()

        applyFriction()
        applyGravity()
    }

    protected fun loopHitAndBounce() {
        Profiler.get().push { "loopHitBounce" }

        bouncePoints.clear()
//        val speedSqr = cyEntity.deltaMovement.lengthSqr()
        var stepPosition = cyEntity.position()
        var stepMovement = cyEntity.deltaMovement

        var hitSomething: Boolean
        var loopTimes = 0
        // Block: { normal, ignore } X Entity: { normal, pierce, ignore }
        if (!collideWithBlocks && !collideWithEntities) {} // penetrate, do nothing
//        else if (speedSqr <= LOW_SPEED_THRESHOLD_SQR) {} // too slow, do nothing
        else
            do {
                if (cyEntity.isRemoved) break
                hitSomething = false

                val stepDestination0 = stepPosition.add(stepMovement)
                var destination = stepDestination0
                var bouncePoint: Vec3? = null
                var blockResult: BlockHitResult? = null
                var bounceDirection: Direction = Direction.NORTH

                // if normal, truncate path
                if (collideWithBlocks) {
                    // get path end-point by checking Block collision
                    blockResult = level.clipIncludingBorder(
                        ClipContext(stepPosition, destination, Block.COLLIDER, Fluid.NONE, cyEntity)
                    )
                    if (blockResult.type != Type.MISS) {
                        // truncate stepDestination
                        destination = blockResult.location
                        bouncePoint = destination
                        bounceDirection = blockResult.direction
                    }
                }

                // handle entity collisions
                if (collideWithEntities) {
                    val margin = HIT_BB_INFLATION + cyEntity.getDimensions(cyEntity.pose).width / 2
                    if (cyEntity.haveFlag(CypherFlags.PIERCE_ENTITY)) {
                        // if tagged pierce, collide all
                        level.getEntities(
                            cyEntity,
                            cyEntity.boundingBox.expandTowards(stepMovement),
                            cyEntity::canHitTarget
                        ).forEach { target ->
                            // there is a trigger call inside whenHit, which may modifies the entity list in section storage.
                            // execute an on-site sub-effect may raise ConcurrentModificationException
                            // it seems we have to extract entities first and go through the list one more time
                            stepPosition.rayCastThen(destination, target.boundingBox, margin) { hitPoint, dir ->
                                whenHitDelegate(EntityHitResult(target, hitPoint), dir)
                            }
                        }
                    } else {
                        // otherwise collide first
                        var nearest = Double.MAX_VALUE
                        var hitEntity: Entity? = null

                        level.forEachEntityWithin(
                            cyEntity,
                            cyEntity.boundingBox.expandTowards(stepMovement),
                            cyEntity::canHitTarget
                        ) { target ->
                            stepPosition.rayCastThen(destination, target.boundingBox, margin) { hitPoint, dir ->
                                val dd: Double = stepPosition.distanceToSqr(hitPoint)
                                if (dd < nearest) {
                                    hitEntity = target
                                    nearest = dd
                                    destination = hitPoint
                                    bounceDirection = dir
                                }
                            }
                        }

                        if (hitEntity != null) {
                            whenHitDelegate(EntityHitResult(hitEntity, destination), bounceDirection)
                            bouncePoint = destination
                        }
                    }
                }

                // bounce from the point
                if (bouncePoint != null) {
                    hitSomething = true
                    if (blockResult?.location == bouncePoint) whenHitDelegate(blockResult, bounceDirection)

                    if (canBounce) {
                        bounceCount++
                        cyEntity.onBounce(cyEntity, bouncePoint, bounceCount)
                        bouncePoints.add(bouncePoint)

                        stepPosition = bouncePoint.add(bounceDirection.unitVec3.scale(1E-7)) // avoid "diving into blocks" bug
                        stepMovement = stepDestination0.subtract(destination).flipByAxis(
                            bounceDirection.axis,
                            cyEntity.getBounceSpeedPenalty()
                        )
                        cyEntity.setPos(stepPosition)
                    }
                }

            } while (hitSomething && canBounce && loopTimes++ <= 8)

        // finalize position
        cyEntity.setPos(stepPosition.add(stepMovement))

        if (loopTimes > 0) {
            cyEntity.deltaMovement = cyEntity.deltaMovement.toSameDire(stepMovement).scale(cyEntity.getBounceSpeedPenalty().pow(loopTimes))
            cyEntity.rotateTowardSpeed(1f) // instant facing direction after bounce
        }

        Profiler.get().pop()
    }



    override fun whenHit(result: HitResult, direction: Direction) = Unit
    override fun whenHitEntity(result: EntityHitResult, direction: Direction) = Unit
    override fun whenHitBlock(result: BlockHitResult, direction: Direction) = Unit
    // in case subclasses want to override whenHit
    // if implementing it here, any subclass of ICypherEntity can't call super.whenHit since its abstract
    // if implementing it inside ICypherEntity as a default function, there will be one more step to link to the function
    // cyEntity.whenHit -> Delegation.whenHit -> interface default
    protected open fun whenHitDelegate(result: HitResult, direction: Direction) {
        cyEntity.whenHit(result, direction)
        cyEntity.onHit(cyEntity, result)
        if (level.isClientSide || result.type == Type.MISS) return
        trigger(TriggerType.COLLISION, result.location)

        if (result is EntityHitResult) {
            whenHitEntityDelegate(result, direction)

            if (!canBounce && cyEntity.noFlag(CypherFlags.PIERCE_ENTITY))
                discardCypher(DiscardReason.HIT_ENTITY)
        }
        else if (result is BlockHitResult) {
            whenHitBlockDelegate(result, direction)

            if (!canBounce)
                discardCypher(DiscardReason.HIT_BLOCK)
        }
    }

    protected open fun whenHitEntityDelegate(
        result: EntityHitResult,
        direction: Direction
    ) {
        cyEntity.whenHitEntity(result, direction)
        val target = result.entity
        if (level.isClientSide) {
            if (cyEntity.noFlag(CypherFlags.SKIP_DAMAGE_CHECK))
                target.hurtClient(cyEntity.getDamageSource())
        }
        else {
            if (cyEntity.noFlag(CypherFlags.SKIP_DAMAGE_CHECK))
                cyEntity.exertDamage(level as ServerLevel, target)
        }
    }

    protected open fun whenHitBlockDelegate(
        result: BlockHitResult,
        direction: Direction
    ) {
        cyEntity.whenHitBlock(result, direction)
        val blockPos = result.blockPos
        if (cyEntity.noFlag(CypherFlags.SILENT))
            this.level.gameEvent(GameEvent.PROJECTILE_LAND, blockPos, Context.of(cyEntity, level.getBlockState(blockPos)))
    }

    override fun whileHomeTarget(target: Entity) {}
}