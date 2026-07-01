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
import net.minecraft.world.entity.Entity
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

    private var _ccMap: MapOfCypherCounts? = null
    override var ccMap: MapOfCypherCounts?
        get() = _ccMap
        set(value) = run { _ccMap = value }
    private var _attributeMap: AttributeMap = HashMap<CypherAttribute, Double>()
    override val attributeMap: Map<CypherAttribute, Double> get() = _attributeMap

    private var _hooks: HookContainer? = null
    override val hooks: HookContainer? get() = _hooks
    override val hooksSharedData = HooksSharedData<CY>()

    private var _trigger = TriggerType.NONE
    override val trigger get() = _trigger
    private var _payload: ShotStateChunk? = null
    override val payload: ShotStateChunk? get() = _payload

    private var isInit: Boolean = false
    var lowSpeedTickCount = 0
        private set
    override var existing
        get() = getAttrOrProjDefault(CypherAttributes.EXISTING).toInt()
        set(value) = run { _attributeMap[CypherAttributes.EXISTING.value()] = CypherAttributes.EXISTING.value().restrictRange(value.toDouble()) }
    protected var bounceCount = 0
    override val canBounce: Boolean get() = bounceCount < bounce
    override val bouncePoints = ArrayList<Vec3>()

    /**
     * if the entity has its own movement logic, set this to false
     * */
    open val moveAsProjectile: Boolean = true
    val collideWithBlocks: Boolean get() = notHaveFlagsAll(CypherFlags.IGNORE_BLOCK, CypherFlags.PENETRATE_WORLD)
    val collideWithEntities: Boolean get() = notHaveFlagsAll(CypherFlags.PENETRATE_WORLD)

    /**
     * used as a factor inside [rotateTowardSpeed],
     * the higher the faster the entity will rotate, to face the direction the deltaMovement is pointed at
     * */
    override val rotationSpeed = 0.25f

    private var owner: Entity? = null
    override fun getOwner(): Entity? = owner
    override fun setOwner(owner: Entity?) = let { this.owner = owner }

    override fun initCypher(cypher: AbstractProjectileCypher<*>, state: ShotStateChunk, node: ProjectileNode?) {
        if (isInit) return
        _attributeMap.initAttributes(state, cypher)
        enabledFlags = state.enabledFlags or cypher.flags
        _hooks = state.hooks
        _ccMap = state.ccMap
        if (node != null) {
            _trigger = node.trigger
            _payload = node.payload
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

    override val positionInitial: Vec3 get() = initPosition ?: Vec3.ZERO
    override val directionInitial: Vec3 get() = initDirection ?: Vec3.ZERO
    private var initPosition: Vec3? = null // due to CyEntity init timing, store direction data and init lazily
    private var initDirection: Vec3? = null
    override fun initDirection(direction: Vec3?) = run { initDirection = direction }
    override fun initDirection(pair: PosDirePair) = run { initDirection = pair.direction; initPosition = pair.position }
    private fun initDirection() {
        initDirection = initDirection?.normalize() ?: cyEntity.owner?.lookAngle?.normalize()
        if (directionInitial != Vec3.ZERO){
            cyEntity.deltaMovement = directionInitial.scale(getAttrOrProjDefault(CypherAttributes.SPEED))
            // FIXME inertia behavior seems strange
        } else {
            cyEntity.deltaMovement = Vec3.ZERO
        }
    }

    protected open fun captureSurroundings() {
        if (cyEntity.firstTick || cyEntity.tickCount and 3 == 3) { // trigger on 1, 3 and then every 4 tick
            val modules = _hooks?.get(CypherBehaviorHooks.ENTITY_SEARCH_BOTH)?.toList() ?: return
            var i = 0
            while (i < modules.size) {
                if (modules[i].first.needSearch(level, cyEntity)) break
                i++
            }
            // if someone need a refresh-search
            if (i < modules.size || needCaptureSurrounding()) {
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
        if (gravity != 0f) cyEntity.deltaMovement = cyEntity.deltaMovement.add(0.0, -(gravity).toDouble(), 0.0)
    }

    protected open fun applyFriction() {
        val f: Float = if (cyEntity.isInWater) underwaterSpeedFactor() * speedFactor else speedFactor
        if (f != 1f) cyEntity.deltaMovement = cyEntity.deltaMovement.scale(f.toDouble())
    }

    /**
     * remember call super to function state hooks [CypherBehaviorHooks.BEFORE_DISCARD_BOTH]
     * */
    protected open fun onBeforeDiscardBoth(reason: DiscardReason) {
        beforeDiscardBoth(reason)
        _hooks?.playHooks(CypherBehaviorHooks.BEFORE_DISCARD_BOTH)
        { h, i -> h.beforeDiscardBoth(level, cyEntity, i, reason) }
    }
    /**
     * remember call super to function state hooks [CypherBehaviorHooks.HIT_ENTITY_BOTH]
     * */
    protected open fun onHitBoth(result: HitResult) {
        hitBoth(result)
        _hooks?.playHooks(CypherBehaviorHooks.HIT_ENTITY_BOTH)
        { h, i -> h.onHitBoth(level, cyEntity, i, result) }
    }
    /**
     * remember call super to function state hooks [CypherBehaviorHooks.FIRST_TICK_BOTH]
     * */
    protected open fun onFirstTickBoth() {
        firstTickBoth()
        _hooks?.playHooks(CypherBehaviorHooks.FIRST_TICK_BOTH)
        { h, i -> h.firstTickBoth(level, cyEntity, i) }
    }
    /**
     * remember call super to function state hooks [CypherBehaviorHooks.TICK_BEHAVIOR_BOTH]
     * change speed / attributes (here) -> finalize movement -> bounce & hit check
     * */
    protected open fun tickBehaviorChangeBoth() {
        tickBehaviorBoth()
        _hooks?.playHooks(CypherBehaviorHooks.TICK_BEHAVIOR_BOTH)
        { h, i -> h.tickBehaviorBoth(level, cyEntity, i) }
    }
    /**
     * remember call super to function state hooks [CypherBehaviorHooks.TICK_MOVEMENT_FINALIZE_BOTH]
     * change speed / attributes -> finalize movement (here) -> bounce & hit check
     * */
    protected open fun tickMovementFinalizeBoth() {
        tickFinalizeMovementBoth()
        _hooks?.playHooks(CypherBehaviorHooks.TICK_MOVEMENT_FINALIZE_BOTH)
        { h, i -> h.finalizeTickMovementBoth(level, cyEntity, i) }
    }
    /**
     * remember call super to function state hooks [CypherBehaviorHooks.ON_BOUNCE_BOTH]
     * */
    protected open fun onBounceBoth(point: Vec3) {
        bounceBoth(point)
        _hooks?.playHooks(CypherBehaviorHooks.ON_BOUNCE_BOTH)
        { h, i -> h.onBounceBoth(level, cyEntity, i, bounceCount, point) }
    }
    /**
     * remember call super to function state hooks [CypherBehaviorHooks.ENTITY_SEARCH_BOTH]
     * */
    protected open fun onCaptureSurroundingBoth(captured: Entity) {
        captureSurroundingBoth(captured)
        // TODO try further optimization, for this is O(m * n)
        _hooks?.playHooks(CypherBehaviorHooks.ENTITY_SEARCH_BOTH)
        { h, i -> h.entitySearchBoth(level, cyEntity, i, captured) }
    }
    /**
     * remember call super to function state hooks []
     * */
    protected open fun onLowSpeedBoth(count: Int) {
        lowSpeedBoth(count)
    }

    private fun releasePayload(posDire: PosDirePair) = _payload?.release(level, cyEntity, cyEntity.owner, posDire, null)
    override fun trigger(type: TriggerType) {
        if (_trigger == TriggerType.NONE || type != _trigger) return
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

    override fun doTick() {
        captureSurroundings()
        if (cyEntity.firstTick) {
            onFirstTickBoth()
        }
        if (cyEntity.tickCount == 20) trigger(TriggerType.TIMER_20)

        hooksSharedData.tick(cyEntity)
        if (moveAsProjectile) projectileTick()

        if (existing <= cyEntity.tickCount && existing != 0) {
            // here's a trick, if player make existing-time exactly equal to 0, projectile will last till the game quit
            discardCypher(DiscardReason.EXPIRE)
        }
    }

    /**
     * handle movement
     * */
    protected open fun projectileTick() {
        tickBehaviorChangeBoth()

        cyEntity.rotateTowardSpeed(rotationSpeed)
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
                    stepMovement = stepDestination0.subtract(destination).flipByDirection(bounceDirection, bounceSpeedPenalty())
                }
            }

        } while (loopContinue && canBounce && loopTimes++ < Int.MAX_VALUE)


        // finalize position
        cyEntity.setPos(stepPosition.add(stepMovement))
        if (loopTimes > 0) {
            cyEntity.deltaMovement = cyEntity.deltaMovement.toSameDire(stepMovement).scale(bounceSpeedPenalty().pow(loopTimes))
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

    override fun whenHit(result: HitResult) {
        onHitBoth(result)
        if (level.isClientSide) return
        trigger(TriggerType.COLLISION)

        // TODO
    }

    override fun canHomeTarget(target: Entity): Boolean {
        return super.canHomeTarget(target) && target != cyEntity.owner
    }
}