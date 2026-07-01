package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.init.mod.CypherBehaviorHooks
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.AttributeOperator.Companion.AttributeMap
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.AttributeOperator.Companion.initAttributes
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.DiscardReason
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation.ICypherEntity.Companion.CAPTURE_SIZE
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation.ICypherEntity.Companion.CLIP_MARGIN
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation.ICypherEntity.Companion.HIT_BB_INFLATION
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation.ICypherEntity.Companion.LOW_SPEED_THRESHOLD
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation.ICypherEntity.Companion.LOW_SPEED_THRESHOLD_SQR
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookContainer
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HooksSharedData
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileNode
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.StateChunkPool
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.TriggerType
import com.github.nahnullscience.cypher_nexus.utility.EntityUtil.rotateTowardSpeed
import com.github.nahnullscience.cypher_nexus.utility.LevelUtil.forEachEntityWithin
import com.github.nahnullscience.cypher_nexus.utility.RayCastUtility
import com.github.nahnullscience.cypher_nexus.utility.RayCastUtility.rayCast
import com.github.nahnullscience.cypher_nexus.utility.RayCastUtility.rayCastThen
import com.github.nahnullscience.cypher_nexus.utility.VectorUtility
import com.github.nahnullscience.cypher_nexus.utility.exception.CypherEntityInitializationException
import com.github.nahnullscience.cypher_nexus.utility.mod.MapOfCypherCounts
import com.github.nahnullscience.cypher_nexus.utility.mod.PosDirePair
import com.github.nahnullscience.cypher_nexus.utility.toSameDire
import com.github.nahnullscience.cypher_nexus.utility.toVec3i
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.Holder
import net.minecraft.util.profiling.Profiler
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.animal.Animal
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.ClipContext.Block
import net.minecraft.world.level.ClipContext.Fluid
import net.minecraft.world.phys.*
import net.minecraft.world.phys.HitResult.Type
import kotlin.jvm.optionals.getOrNull
import kotlin.math.pow


open class CypherEntityBasics <CY> : ICypherEntity where CY : Entity, CY : ICypherEntity {
    companion object {
        /**
         *
         * */
        fun Vec3.flipByDirection(dir: Direction, factor: Double = 1.0): Vec3 {
            return when(dir) {
                Direction.DOWN, Direction.UP -> multiply(1.0, -factor, 1.0)
                Direction.NORTH, Direction.SOUTH -> multiply(1.0, 1.0, -factor)
                Direction.WEST, Direction.EAST -> multiply(-factor, 1.0, 1.0)
            }
        }

    }

    private var _cyEntity: CY? = null
    val cyEntity: CY get() = _cyEntity ?:
    throw CypherEntityInitializationException("CypherEntityDelegation failed to initialize! make sure call #initEntity before it's adding to world!")
    val level get() = cyEntity.level()
    override val cypherHolder: Holder<out AbstractProjectileCypher<*>> get() = cyEntity.cypherHolder // FIXME this may lead to infinite loop

    /** no flag by default */
    override var enabledFlags = CypherFlags.fromFlags()

    protected var ccMap: MapOfCypherCounts? = null
    override fun ccMap(): MapOfCypherCounts? = ccMap

    protected val attributeMap: AttributeMap = HashMap<CypherAttribute, Double>()
    override fun attributeMap(): Map<CypherAttribute, Double> = attributeMap

    protected var hooks: HookContainer? = null
    override fun hooks(): HookContainer? = hooks

    override val hooksSharedData = HooksSharedData<CY>()
    override fun hooksSharedData(): HooksSharedData<CY> = hooksSharedData

    protected var trigger = TriggerType.NONE
    override fun triggerType(): TriggerType = trigger

    protected var payload: ShotStateChunk? = null
    override fun payload(): ShotStateChunk? = payload

    override fun getDirectionInitial(): Vec3 = _initDirection ?: Vec3.ZERO
    override fun getPositionInitial(): Vec3  = _initPosition ?: Vec3.ZERO
    override fun getExisting(): Int = attributeOrDefault(CypherAttributes.EXISTING).toInt()
    override fun getSpeed(): Double = attributeOrDefault(CypherAttributes.EXISTING)
    override fun getBounce(): Int = attributeOrDefault(CypherAttributes.BOUNCE).toInt()
    override fun getGravityFactor(): Float = attributeOrDefault(CypherAttributes.GRAVITY_FACTOR).toFloat()
    override fun getSpeedFactor(): Float = 1f - attributeOrDefault(CypherAttributes.FRICTION_FACTOR).toFloat()
    override fun getEffectRadius(): Double = attributeOrDefault(CypherAttributes.EFFECT_RADIUS)
    override fun getUnderwaterSpeedFactor() = 0.8f
    override fun getInWallSpeedFactor() = 0.5f
    override fun getBounceSpeedPenalty() = 0.9
    override fun getRotationSpeed(): Float = 0.25f

    override fun attribute(attr: CypherAttribute): Double? = attributeMap[attr]
    override fun attribute(holer: Holder<CypherAttribute>): Double? = attribute(holer.value())
    override fun attributeOrDefault(attr: CypherAttribute): Double = attributeMap[attr] ?: cypher.getAttrBaseOrDefault(attr)
    override fun attributeOrDefault(holer: Holder<CypherAttribute>): Double = attributeOrDefault(holer.value())

    override fun needCaptureSurrounding(): Boolean = false

    protected var bounceCount = 0
    override val canBounce: Boolean get() = bounceCount < cyEntity.getBounce()
    override val bouncePoints = ArrayList<Vec3>()
    override val bouncedThisTick: Boolean get() = bouncePoints.isNotEmpty()

    protected var isInit: Boolean = false
    protected var lowSpeedTickCount = 0

    protected val collideWithBlocks: Boolean get() = notHaveFlagsAll(CypherFlags.IGNORE_BLOCK, CypherFlags.PENETRATE_WORLD)
    protected val collideWithEntities: Boolean get() = notHaveFlagsAll(CypherFlags.PENETRATE_WORLD)
    /**
     * if the entity has its own movement logic, set this to false
     * */
    open val moveAsProjectile: Boolean = true

    private var owner: Entity? = null
    override fun getOwner(): Entity? = owner
    override fun setOwner(owner: Entity?) = let { this.owner = owner }

    override fun initCypher(cypher: AbstractProjectileCypher<*>, state: ShotStateChunk, node: ProjectileNode?) {
        if (isInit) return
        attributeMap.initAttributes(state, cypher)
        enabledFlags = state.enabledFlags or cypher.flags
        hooks = state.hooks
        ccMap = state.ccMap
        if (node != null) {
            trigger = node.trigger
            payload = node.payload
        }

        isInit = true
    }
    override fun initCypher(cypher: AbstractProjectileCypher<*>, map: MapOfCypherCounts?) {
        if (map == null) return
        val state = StateChunkPool.getOrCreateStateChunk(map)
        initCypher(cypher, state, null)
    }
    @Suppress("UNCHECKED_CAST")
    override fun <E> initEntity (cy: E) where E : Entity, E : ICypherEntity {
        _cyEntity = cy as CY
        initDirection()
    }

    private var _initPosition: Vec3? = null // due to CyEntity init timing, remember direction data and init later
    private var _initDirection: Vec3? = null
    override fun initDirection(direction: Vec3?) = run { _initDirection = direction }
    override fun initDirection(pair: PosDirePair) = run { _initDirection = pair.direction; _initPosition = pair.position }


    private fun initDirection() {
        if (level.isClientSide) return
        _initDirection = _initDirection?.normalize() ?: cyEntity.owner?.lookAngle?.normalize()
        if (cyEntity.getDirectionInitial() != Vec3.ZERO){
            cyEntity.deltaMovement = cyEntity.getDirectionInitial().scale(cyEntity.getSpeed())
            // FIXME inertia behavior seems strange
        } else {
            cyEntity.deltaMovement = Vec3.ZERO
        }
    }

    protected open fun captureSurroundings() {
        if (cyEntity.firstTick || cyEntity.tickCount and 3 == 3) { // trigger on 1, 3 and then every 4 tick
            val modules = hooks?.get(CypherBehaviorHooks.ENTITY_SEARCH_BOTH)?.toList() ?: return
            var i = 0
            while (i < modules.size) {
                if (modules[i].first.needSearch(level, cyEntity)) break
                i++
            }
            // if someone need a refresh-search
            if (i < modules.size || cyEntity.needCaptureSurrounding()) {
                val entities = level.getEntities(
                    cyEntity,
                    cyEntity.boundingBox.inflate(CAPTURE_SIZE)
                ) { entity -> entity !is ICypherEntity }

//                println("capture $entities")
                for (entity in entities) {
                    onCaptureSurroundingBoth(entity)
                }
            }
        }
    }

    protected open fun applyGravity() {
        if (cyEntity.getGravityFactor() != 0f)
            cyEntity.deltaMovement = cyEntity.deltaMovement.add(0.0, -(cyEntity.getGravityFactor()).toDouble(), 0.0)
    }

    protected open fun applyFriction() {
        val f: Float =
            if (cyEntity.isInWater) cyEntity.getUnderwaterSpeedFactor() * cyEntity.getSpeedFactor()
            else cyEntity.getSpeedFactor()
        if (f != 1f) cyEntity.deltaMovement = cyEntity.deltaMovement.scale(f.toDouble())
    }

    // TODO unify hook names
    /**
     * remember call super to function state hooks [CypherBehaviorHooks.BEFORE_DISCARD_BOTH]
     * */
    protected open fun onBeforeDiscardBoth(reason: DiscardReason) {
        cyEntity.beforeDiscardBoth(reason)
        hooks?.playHooks(CypherBehaviorHooks.BEFORE_DISCARD_BOTH)
        { h, i -> h.beforeDiscardBoth(level, cyEntity, i, reason) }
    }
    /**
     * remember call super to function state hooks [CypherBehaviorHooks.HIT_ENTITY_BOTH]
     * */
    protected open fun onHitBoth(result: HitResult) {
        cyEntity.hitBoth(result)
        hooks?.playHooks(CypherBehaviorHooks.HIT_ENTITY_BOTH)
        { h, i -> h.onHitBoth(level, cyEntity, i, result) }
    }
    /**
     * remember call super to function state hooks [CypherBehaviorHooks.FIRST_TICK_BOTH]
     * */
    protected open fun onFirstTickBoth() {
        cyEntity.firstTickBoth()
        hooks?.playHooks(CypherBehaviorHooks.FIRST_TICK_BOTH)
        { h, i -> h.firstTickBoth(level, cyEntity, i) }
    }
    /**
     * remember call super to function state hooks [CypherBehaviorHooks.TICK_BEHAVIOR_BOTH]
     * change speed / attributes (here) -> finalize movement -> bounce & hit check
     * */
    protected open fun tickBehaviorChangeBoth() {
        cyEntity.tickBehaviorBoth()
        hooks?.playHooks(CypherBehaviorHooks.TICK_BEHAVIOR_BOTH)
        { h, i -> h.tickBehaviorBoth(level, cyEntity, i) }
    }
    /**
     * remember call super to function state hooks [CypherBehaviorHooks.TICK_MOVEMENT_FINALIZE_BOTH]
     * change speed / attributes -> finalize movement (here) -> bounce & hit check
     * */
    protected open fun tickMovementFinalizeBoth() {
        cyEntity.tickFinalizeMovementBoth()
        hooks?.playHooks(CypherBehaviorHooks.TICK_MOVEMENT_FINALIZE_BOTH)
        { h, i -> h.finalizeTickMovementBoth(level, cyEntity, i) }
    }
    /**
     * remember call super to function state hooks [CypherBehaviorHooks.ON_BOUNCE_BOTH]
     * */
    protected open fun onBounceBoth(point: Vec3) {
        cyEntity.bounceBoth(point)
        hooks?.playHooks(CypherBehaviorHooks.ON_BOUNCE_BOTH)
        { h, i -> h.onBounceBoth(level, cyEntity, i, bounceCount, point) }
    }
    /**
     * remember call super to function state hooks [CypherBehaviorHooks.ENTITY_SEARCH_BOTH]
     * */
    protected open fun onCaptureSurroundingBoth(captured: Entity) {
        cyEntity.captureSurroundingBoth(captured)
        // TODO try further optimization, for this is O(m * n)
        hooks?.playHooks(CypherBehaviorHooks.ENTITY_SEARCH_BOTH)
        { h, i -> h.entitySearchBoth(level, cyEntity, i, captured) }
    }
    /**
     * remember call super to function state hooks []
     * */
    protected open fun onLowSpeedBoth(count: Int) {
        cyEntity.lowSpeedBoth(count)
    }

    private fun releasePayload(posDire: PosDirePair) = payload?.release(level, cyEntity, cyEntity.owner, posDire, null)
    override fun trigger(type: TriggerType) {
        if (trigger == TriggerType.NONE || type != trigger) return
        val to = when(type) {
            // TODO apply a random offset
            TriggerType.COLLISION -> PosDirePair(cyEntity.position(), cyEntity.deltaMovement.reverse())
            else -> PosDirePair(cyEntity.position(), cyEntity.deltaMovement)
        }
        releasePayload(to)
    }

    override fun discardCypher(reason: DiscardReason) {
        if (level.isClientSide) return
        trigger(TriggerType.DEATH)
        when(reason){
            DiscardReason.ERASE -> {}
            else -> {
                onBeforeDiscardBoth(reason)
            }
        }
        level.broadcastEntityEvent(cyEntity, 3)
        cyEntity.discard()
    }

    override fun beforeDiscardBoth(reason: DiscardReason) {}

    override fun hitBoth(result: HitResult) {}

    override fun firstTickBoth() {}

    override fun tickBehaviorBoth() {}

    override fun tickFinalizeMovementBoth() {}

    override fun bounceBoth(bouncePoint: Vec3) {}

    override fun captureSurroundingBoth(captured: Entity) {}

    override fun lowSpeedBoth(count: Int) {
        if (count < 40) return
        if (cyEntity.getSpeed() > LOW_SPEED_THRESHOLD) {
            discardCypher(DiscardReason.LOW_SPEED)
        }
    }

    override fun doTick() {
        Profiler.get().push("cypherEntityTick")

        captureSurroundings()
        if (cyEntity.firstTick) {
            onFirstTickBoth()
        }
        if (cyEntity.tickCount == 20) trigger(TriggerType.TIMER_20)

        hooksSharedData.tick(cyEntity)
        if (moveAsProjectile) projectileTick()

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
        tickBehaviorChangeBoth()

        cyEntity.rotateTowardSpeed(cyEntity.getRotationSpeed())
        applyFriction()
        applyGravity()
        tickMovementFinalizeBoth()

        if (cyEntity.deltaMovement.lengthSqr() <= LOW_SPEED_THRESHOLD_SQR) {
            onLowSpeedBoth(lowSpeedTickCount ++)
        } else lowSpeedTickCount = 0

        /*
         * deltaMovement: the movement for the "next tick", client smooth animation relay on this
         * an AABB check is used everyTick every vanilla projectile, sounds outrageous, but is ok in performance
         * */
        loopHitAndBounce()
    }

    protected fun loopHitAndBounce() {
        bouncePoints.clear()
        val speedSqr = cyEntity.deltaMovement.lengthSqr()
        var stepPosition = cyEntity.position()
        var stepMovement = cyEntity.deltaMovement

        var loopContinue = false
        var loopTimes = 0
        // Block: { normal, ignore } X Entity: { normal, pierce, ignore }
        // 6 situations in total
        if (!collideWithBlocks && !collideWithEntities) // penetrate, do nothing
        else if (speedSqr <= LOW_SPEED_THRESHOLD_SQR) // too slow, do nothing
        else
        do {
            if (cyEntity.isRemoved) return
            // TODO use BB based on step position
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
                if (haveFlag(CypherFlags.PIERCE_ENTITY)) {
                    // if pierce, collide all
                    level.forEachEntityWithin(
                        cyEntity,
                        cyEntity.boundingBox.expandTowards(stepMovement),
                        cyEntity::canHitTarget
                    ) { target ->
                        val hitPoint = stepPosition.rayCast(destination, target.boundingBox, HIT_BB_INFLATION)
                        if (hitPoint != null) {
                            whenHit(EntityHitResult(target, hitPoint))
                        }
                    }
                } else {
                    // otherwise collide first
                    var nearest = Double.MAX_VALUE
                    var hitEntityTmp: Entity? = null

                    /**
                     * [Entity.makeBoundingBox]
                     * TODO resize bounding box to fit Effect-Radius attribute
                     * */
                    level.forEachEntityWithin(
                        cyEntity,
                        cyEntity.boundingBox.expandTowards(stepMovement),
                        cyEntity::canHitTarget
                    ) { target ->
                        /**
                         * TODO replace [HIT_BB_INFLATION] with Effect-Radius-friendly cy-entity-bb size
                         * */
                        stepPosition.rayCastThen(destination, target.boundingBox, HIT_BB_INFLATION) { hitPoint, dir ->
                            val dd: Double = stepPosition.distanceToSqr(hitPoint)
                            if (dd < nearest) {
                                hitEntityTmp = target
                                nearest = dd
                                destination = hitPoint
                                bounceDirection = dir
                            }
                        }
                    }

                    if (hitEntityTmp != null) {
                        whenHit(EntityHitResult(hitEntityTmp, destination))
                        bouncePoint = destination
                    }
                }
            }

            // bounce from the point
            if (bouncePoint != null) {
                loopContinue = true
                if (blockResult?.location == bouncePoint) whenHit(blockResult)

                if (canBounce) {
                    bounceCount++
                    onBounceBoth(bouncePoint)
                    bouncePoints.add(bouncePoint)

                    stepPosition = bouncePoint.add(bounceDirection.unitVec3.scale(0.01))
                    stepMovement = stepDestination0.subtract(destination).flipByDirection(
                        bounceDirection,
                        cyEntity.getBounceSpeedPenalty()
                    )
                }
            }

        } while (loopContinue && canBounce && loopTimes++ < Int.MAX_VALUE)


        // finalize position
        cyEntity.setPos(stepPosition.add(stepMovement))
        if (loopTimes > 0) {
            cyEntity.deltaMovement = cyEntity.deltaMovement.toSameDire(stepMovement).scale(cyEntity.getBounceSpeedPenalty().pow(loopTimes))
        }
    }

    /**
     * handle bounce movement logic and trigger #onHit.
     * @return a pair of lastHitPoint and deltaMove for the last leg, current #position and #deltaMovement if no bounce.
     * */
    protected fun bounceLoop(hitResult: HitResult): Pair<Vec3, Vec3> {
        val defaultReturn = Pair(cyEntity.position(), cyEntity.deltaMovement)
        if (hitResult.type == Type.MISS) return defaultReturn
        var hitResultStep = hitResult
        var startPosStep = cyEntity.position()
        var deltaMoveStep = cyEntity.deltaMovement

        do {
            // EventHooks.onProjectileImpact(this, hitResultStep), maybe get a result from broadcast
//            if (!level().isClientSide) println("loop$bounce: \n$hitResultStep\n$startPosStep\n$deltaMoveStep")

            // FIXME image a situation that one proj with bounce can pierce block but can not pierce entity, it should bounce back when an entity stand behind a wall
//            onHit(hitResultStep) // or hitTargetOrDeflectSelf(hitResult)
            val canPierce = hitResultStep is BlockHitResult && haveFlag(CypherFlags.IGNORE_BLOCK)
                    || hitResultStep is EntityHitResult && haveFlag(CypherFlags.PIERCE_ENTITY)
            if (!canBounce || canPierce) break

            val targetBox = when(hitResultStep) {
                is EntityHitResult -> hitResultStep.entity.boundingBox.inflate(CLIP_MARGIN.toDouble())
                is BlockHitResult -> AABB(hitResultStep.blockPos)
                else -> AABB(BlockPos(hitResultStep.location.toVec3i()))
            }
            val hitPoint = targetBox.clip(startPosStep, startPosStep.add(deltaMoveStep)).getOrNull()

//            if (!level().isClientSide) println("hitPoint $hitPoint \naabb $targetBox")

            val direction = VectorUtility.getDireFromHit(hitPoint, targetBox)
            if (hitPoint == null || direction == null) { // this block should not be reached
                if (!level.isClientSide) CypherNexus.LOGGER.error("hitPoint == null || direction == null\n$direction")
                return defaultReturn
            }

            // do reflect
            deltaMoveStep = hitPoint.vectorTo(startPosStep.add(deltaMoveStep))
            deltaMoveStep = when(direction) {
                Direction.DOWN, Direction.UP -> deltaMoveStep.multiply(1.0, -1.0, 1.0)
                Direction.NORTH, Direction.SOUTH -> deltaMoveStep.multiply(1.0, 1.0, -1.0)
                Direction.WEST, Direction.EAST -> deltaMoveStep.multiply(-1.0, 1.0, 1.0)
            }
            startPosStep = hitPoint
            bounceCount++
            bouncePoints.add(hitPoint)

            // handle next bounce
            hitResultStep = RayCastUtility.getProjectileHitResult(startPosStep, cyEntity, ::canHitTarget, deltaMoveStep, level, CLIP_MARGIN)

        } while (hitResultStep.type != Type.MISS)

        return Pair(startPosStep, deltaMoveStep)
    }

    override fun canHitTarget(target: Entity): Boolean {
        if (!target.canBeHitByProjectile()) {
            return false // vanilla logic, for item-entities
        }
        if (cyEntity.owner == target && notHaveFlag(CypherFlags.HURT_OWNER)) return false
        return true
    }

    override fun whenHit(result: HitResult) {
        onHitBoth(result)
        if (level.isClientSide) return
        trigger(TriggerType.COLLISION)

        // TODO
    }

    override fun canHomeTarget(target: Entity): Boolean {
        return canHitTarget(target)
                && target is LivingEntity
                && target !is Animal
//                && target !is ItemEntity
                && !target.isInvisible
                && target.isAlive
                && target != owner
                && target != cyEntity.owner
    }

    override fun whileHomeTarget(target: Entity) {}
}