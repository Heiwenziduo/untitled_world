package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation

import com.github.nahnullscience.cypher_nexus.init.mod.Cyphers
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.DiscardReason
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ExplosionSettings
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity.Companion.HIT_BB_INFLATION
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity.Companion.KINETIC_DAMAGE_SPEED_SQR
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity.Companion.LOW_SPEED_THRESHOLD_SQR
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity.Companion.collideWithBlocks
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity.Companion.collideWithEntities
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity.Companion.exertDamage
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntityAttributeAccessor.Companion.getBounce
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntityAttributeAccessor.Companion.getExisting
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntityAttributeAccessor.Companion.getGravityFactor
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntityAttributeAccessor.Companion.getSpeedFactor
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntityLogicContext
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileNode
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.TriggerType
import com.github.nahnullscience.cypher_nexus.utility.*
import com.github.nahnullscience.cypher_nexus.utility.linear_space.CoordinateDefinition
import com.github.nahnullscience.cypher_nexus.utility.linear_space.PosDirePair
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.profiling.Profiler
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.ClipContext.Block
import net.minecraft.world.level.ClipContext.Fluid
import net.minecraft.world.level.Level
import net.minecraft.world.level.gameevent.GameEvent
import net.minecraft.world.level.gameevent.GameEvent.Context
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.HitResult.Type
import net.minecraft.world.phys.Vec3
import kotlin.math.pow

open class CEPhysicsBasics <CE> : ICEPhysics<CE> where CE : Entity, CE : ICypherEntity {
    protected lateinit var ce: CE
    protected val level get() = ce.level()
    protected val random get() = ce.random

    override var tickStartSpeedSqr: Double = 0.0
    override var capturedInitialSpeedSqr: Double = 0.0

    override var triggerType = TriggerType.NONE
    override var payload: ShotStateChunk? = null

    protected var bounceCount = 0
    override val canBounce: Boolean get() = bounceCount < ce.getBounce()
    override val bouncePoints = ArrayList<Vec3>()
    override val bouncedThisTick: Boolean get() = bouncePoints.isNotEmpty()

    protected var lowSpeedTickCount = 0
    protected var highElevationTickCount = 0

    protected var hitEntityInvulnerabilityMap: Int2IntOpenHashMap? = null
        private set

    protected var explosion: ExplosionSettings<*>? = null


    override fun initCypher(cypher: AbstractProjectileCypher<*>, shotState: ShotStateChunk?, node: ProjectileNode?) {
        if (node != null) {
            triggerType = node.trigger
            payload = node.payload
        }
    }

    override fun initEntity(ce: CE) {
        this@CEPhysicsBasics.ce = ce
        ce.noPhysics = ce.noFlag(CypherFlags.PHYSICS_SOLID)

        // if can hit multiple target...
        if (ce.hasFlagsAny(CypherFlags.PHYSICS_SOLID, CypherFlags.PIERCE_ENTITY) || ce.canBounce) {
            hitEntityInvulnerabilityMap = Int2IntOpenHashMap()
        }

        ce.initExplosion().also { explosion = it }
    }


    protected open fun applyGravity() {
        if (ce.getGravityFactor() != 0.0)
            ce.deltaMovement = ce.deltaMovement.add(0.0, -ce.getGravityFactor(), 0.0)
    }

    protected open fun applyFriction() {
        val f: Double =
            if (ce.isInWater) ce.getUnderwaterSpeedFactor() * ce.getSpeedFactor()
            else ce.getSpeedFactor()
        if (f >= 1.0 && tickStartSpeedSqr >= 64.0) return
        else ce.deltaMovement *= f
    }

    override fun trigger(coordinate: CoordinateDefinition, releaseTo: PosDirePair) {
        payload?.release(
            level,
            coordinate,
            releaseTo,
            ce,
            ce.owner
        )
    }
    protected open fun handleTrigger(type: TriggerType, releasePoint: Vec3, speedDir: Vec3) {
        if (level.isClientSide || triggerType == TriggerType.NONE || type != triggerType) return
        ce.whenFace(speedDir, false) { front, left ->
            val co = CoordinateDefinition(front, left)
            val po = PosDirePair(releasePoint, speedDir)
            trigger(co, po)
        }
    }
    protected open fun handleCollisionTrigger(direction: Direction, releasePoint: Vec3, speedDir: Vec3) {
        if (level.isClientSide || triggerType != TriggerType.COLLISION) return

        val co = CoordinateDefinition.faceDirectionWithUpVector(direction, speedDir) {
            direction.axis.randomPerpendicularNormal(random)
        }
        val po = PosDirePair(releasePoint + direction.unitVec3 * 0.1, direction.unitVec3)
        trigger(co, po)
    }


    override fun discardCypher(reason: DiscardReason) {
        explode(level, ce.x, ce.y, ce.z)
        handleTrigger(TriggerType.DEATH, ce.position(), ce.deltaMovement)

        if (level.isClientSide) { return }
        val level = level as ServerLevel

        when(reason){
            DiscardReason.ERASE -> {}
            else -> {
                ce.beforeDiscardServer(ce, level, reason)
                ce.steerer.discard(ce, reason)
            }
        }
        level.broadcastEntityEvent(ce, 3)
        ce.discard()
    }


    override fun explode(
        level: Level,
        x: Double,
        y: Double,
        z: Double,
        factor: Float
    ): Boolean {
        if (ce.ccMap?.containsKey(Cyphers.REMOVE_EXPLOSION) == true) return false
        explosion?.let {
            if (level is ServerLevel) it.explode(level, x, y, z, factor)
            ce.onExplode(ce)
            return true
        }
        return false
    }

    override fun doTick() {
        Profiler.get().push { "cypherEntityTick" }

        if (ce.tickCount == 1) {
            ce.onFirstTick(ce)
            ce.steerer.init(ce)
        }

        tickStartSpeedSqr = ce.deltaMovement.lengthSqr()

        if (ce.tickCount == 3) {
            capturedInitialSpeedSqr = tickStartSpeedSqr
        }

        if (triggerType.timer == ce.tickCount)
            handleTrigger(triggerType, ce.position(), ce.deltaMovement)

        // try capture on tick 1, and then every 4 ticks
        if (ce.tickCount == 1 || (ce.tickCount - 2) and 3 == 3) ce.captureSurroundings(ce)

//        cyEntity.hooksSharedData.tick(cyEntity)
//        if (moveAsProjectile)
        projectileTick()

        if (ce.getExisting() <= ce.tickCount && ce.getExisting() != 0) {
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
        ce.onTick(ce)
        ce.steerer.tick(ce)

        ce.rotateTowardSpeed(ce.getRotationSpeed())
        ce.finalizeTickMovement(ce)
        ce.steerer.tickSpeedOverride(ce)

        onLowSpeedCheck()
        onHighSkyCheck()

        loopHitAndBounce()

        applyFriction()
        applyGravity()
    }

    protected fun loopHitAndBounce() {
        Profiler.get().push { "loopHitBounce" }

        bouncePoints.clear()
//        val speedSqr = cyEntity.deltaMovement.lengthSqr()
        var stepPosition = ce.position()
        var stepMovement = ce.deltaMovement

        var hitSomething: Boolean
        var loopTimes = 0
        // Block: { normal, ignore } X Entity: { normal, pierce, ignore }
        if (!ce.collideWithBlocks && !ce.collideWithEntities) {} // penetrate, do nothing
//        else if (speedSqr <= LOW_SPEED_THRESHOLD_SQR) {} // too slow, do nothing
        else
            do {
                if (ce.isRemoved) break
                hitSomething = false

                val stepDestination0 = stepPosition.add(stepMovement)
                var destination = stepDestination0
                var bouncePoint: Vec3? = null
                var blockResult: BlockHitResult? = null
                var bounceDirection: Direction? = null

                // if normal, truncate path
                if (ce.collideWithBlocks) {
                    // get path end-point by checking Block collision
                    blockResult = level.clipIncludingBorder(
                        ClipContext(stepPosition, destination, Block.COLLIDER, Fluid.NONE, ce)
                    )
                    if (blockResult.type != Type.MISS) {
                        // truncate stepDestination
                        destination = blockResult.location
                        bouncePoint = destination
                        bounceDirection = blockResult.direction
                    }
                }

                // handle entity collisions
                if (ce.collideWithEntities) {
                    val margin = HIT_BB_INFLATION + ce.getDimensions(ce.pose).width / 2
                    if (ce.hasFlag(CypherFlags.PIERCE_ENTITY)) {
                        // if tagged pierce, collide all
                        level.getEntities(
                            ce,
                            ce.boundingBox.expandToAtMost(stepMovement, 16.0),
                            ce::canHitTarget
                        ).forEach { target ->
                            // there is a trigger call inside whenHit, which may modifies the entity list in section storage.
                            // execute an on-site sub-effect may raise ConcurrentModificationException
                            // it seems we have to extract entities first and go through the list one more time
                            if (canHitTargetAtThisTick(target))
                            stepPosition.rayCastThen(destination, target.boundingBox, margin) { hitPoint, dir ->
                                whenHitDelegate(EntityHitResult(target, hitPoint), stepMovement, dir)
                            }
                        }
                    } else {
                        // otherwise collide first
                        var nearest = Double.MAX_VALUE
                        var hitEntity: Entity? = null

                        level.forEachEntityWithin(
                            ce,
                            ce.boundingBox.expandToAtMost(stepMovement, 16.0),
                            ce::canHitTarget
                        ) { target ->
                            if (canHitTargetAtThisTick(target))
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
                            whenHitDelegate(EntityHitResult(hitEntity, destination), stepMovement, bounceDirection)
                            bouncePoint = destination
                        }
                    }
                }

                // bounce from the point
                if (bouncePoint != null) {
                    hitSomething = true
                    if (blockResult?.location == bouncePoint) whenHitDelegate(blockResult, stepMovement, bounceDirection)

                    if (canBounce) {
                        bounceCount++
                        bouncePoints.add(bouncePoint)
                        val bd = bounceDirection ?: stepMovement.mostAlignedDirection()
                        onBounceDelegate(bouncePoint, bounceCount, bd)

                        stepPosition = bouncePoint.add(bd.unitVec3.scale(1E-4)) // avoid "diving into blocks" bug
                        stepMovement = stepDestination0.subtract(destination).flipByAxis(
                            bd.axis,
                            ce.getBounceSpeedDegrade()
                        )
                        ce.setPos(stepPosition)
                        ce.needsSync = true
                    }
                }

            } while (hitSomething && canBounce && loopTimes++ <= 16)

        // finalize position
        ce.setPos(stepPosition.add(stepMovement))

        if (loopTimes > 0) {
            ce.deltaMovement = ce.deltaMovement.toSameDire(stepMovement).let {
                val factor = ce.getBounceSpeedDegrade()
                if (factor != 1.0) it.scale(factor.pow(loopTimes))
                else it
            }
            ce.rotateTowardSpeed(1f) // instant facing direction after bounce
        }

        Profiler.get().pop()
    }

    /**
     * assume [target] is a valid one through [ICypherEntityLogicContext.canHitTarget]
     * */
    open fun canHitTargetAtThisTick(target: Entity): Boolean {
        return hitEntityInvulnerabilityMap?.get(target.id)?.let { it <= ce.tickCount } ?: true
    }


    protected open fun onBounceDelegate(bouncePoint: Vec3, bounceCount: Int, bounceSurface: Direction) {
        ce.onBounce(ce, bouncePoint, bounceSurface, bounceCount)
    }


    // in case subclasses want to override whenHit
    // if implementing it here, any subclass of ICypherEntity can't call super.whenHit since its abstract
    // if implementing it inside ICypherEntity as a default function, there will be one more step to link to the function
    // cyEntity.whenHit -> Delegation.whenHit -> interface default
    protected open fun whenHitDelegate(result: HitResult, stepMove: Vec3, direction: Direction?) {
        if (result.type == Type.MISS) return
        val dir = direction ?: run {
            if (tickStartSpeedSqr > KINETIC_DAMAGE_SPEED_SQR) stepMove.mostAlignedDirection()
            else return
        }

        ce.whenHit(result, dir)
        ce.onHit(ce, result)
        handleCollisionTrigger(dir, result.location, stepMove)

        if (result is EntityHitResult) {
            whenHitEntityDelegate(result, dir)

            if (!canBounce && ce.noFlag(CypherFlags.PIERCE_ENTITY))
                discardCypher(DiscardReason.HIT_ENTITY)
        }
        else if (result is BlockHitResult) {
            whenHitBlockDelegate(result, dir)

            if (!canBounce)
                discardCypher(DiscardReason.HIT_BLOCK)
        }
    }

    protected open fun whenHitEntityDelegate(
        result: EntityHitResult,
        direction: Direction
    ) {
        ce.whenHitEntity(result, direction)
        val target = result.entity

        hitEntityInvulnerabilityMap?.let {
            var next = ce.tickCount + ce.getHitSameTargetTickNeeds()
            if (ce.noFlag(CypherFlags.PIERCE_ENTITY)) next += 10 // for bounce only
            it.put(target.id, next)
        }

        if (level.isClientSide) {
            if (ce.noFlag(CypherFlags.SKIP_DAMAGE_CHECK))
                target.hurtClient(ce.getDamageSource())
        }
        else {
            if (ce.noFlag(CypherFlags.SKIP_DAMAGE_CHECK))
                ce.exertDamage(level as ServerLevel, target)
        }
    }

    protected open fun whenHitBlockDelegate(
        result: BlockHitResult,
        direction: Direction
    ) {
        ce.whenHitBlock(result, direction)
        val blockPos = result.blockPos
        if (ce.noFlag(CypherFlags.SILENT))
            this.level.gameEvent(GameEvent.PROJECTILE_LAND, blockPos, Context.of(ce, level.getBlockState(blockPos)))
    }


    protected open fun onLowSpeedCheck() {
        if (ce.noFlagsNone(CypherFlags.PHYSICS_SOLID, CypherFlags.MOTION_FOLLOWS_OWNER) &&
            capturedInitialSpeedSqr > LOW_SPEED_THRESHOLD_SQR &&
            tickStartSpeedSqr <= LOW_SPEED_THRESHOLD_SQR)
        {
            if (++lowSpeedTickCount > 7) ce.discardCypher(DiscardReason.LOW_SPEED)
        }
        else lowSpeedTickCount = 0
    }

    protected open fun onHighSkyCheck() {
        if (ce.y > 4000 && ce.deltaMovement.y > 0) {
            if (++highElevationTickCount > 20) ce.discardCypher(DiscardReason.FEAR_OF_HEIGHTS)
        }
        else highElevationTickCount = 0
    }
}