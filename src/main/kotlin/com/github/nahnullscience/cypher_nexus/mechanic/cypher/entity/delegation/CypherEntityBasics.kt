package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation

import com.github.nahnullscience.cypher_nexus.init.data_driven.ModDamageTypes.CYPHER_DEFAULT
import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.init.mod.CypherBehaviorHooks
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.AttributeOperator.Companion.AttributeMap
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.AttributeOperator.Companion.initAttributes
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.DiscardReason
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation.ICypherEntity.Companion.CAPTURE_SIZE
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation.ICypherEntity.Companion.HIT_BB_INFLATION
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation.ICypherEntity.Companion.LOW_SPEED_THRESHOLD
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation.ICypherEntity.Companion.LOW_SPEED_THRESHOLD_SQR
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation.ICypherEntity.Companion.exertDamage
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookContainer
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HooksSharedData
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileNode
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.StateChunkPool
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.TriggerType
import com.github.nahnullscience.cypher_nexus.utility.EntityUtil.rotateTowardSpeed
import com.github.nahnullscience.cypher_nexus.utility.LevelUtil.forEachEntityWithin
import com.github.nahnullscience.cypher_nexus.utility.exception.CypherEntityInitializationException
import com.github.nahnullscience.cypher_nexus.utility.mod.MapOfCypherCounts
import com.github.nahnullscience.cypher_nexus.utility.mod.PosDirePair
import com.github.nahnullscience.cypher_nexus.utility.rayCastThen
import com.github.nahnullscience.cypher_nexus.utility.toSameDire
import net.minecraft.core.Direction
import net.minecraft.core.Holder
import net.minecraft.core.registries.Registries
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.profiling.Profiler
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntitySelector
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.animal.Animal
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.ClipContext.Block
import net.minecraft.world.level.ClipContext.Fluid
import net.minecraft.world.level.gameevent.GameEvent
import net.minecraft.world.level.gameevent.GameEvent.Context
import net.minecraft.world.phys.*
import net.minecraft.world.phys.HitResult.Type
import kotlin.math.pow


open class CypherEntityBasics <CE> : ICypherEntity where CE : Entity, CE : ICypherEntity {
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

    private var _cyEntity: CE? = null
    protected val cyEntity: CE get() = _cyEntity ?:
    throw CypherEntityInitializationException("CypherEntityDelegation failed to initialize! make sure call #initEntity before it's adding to world!")
    protected val level get() = cyEntity.level()
    protected val random get() = cyEntity.random
    override val cypherHolder: Holder<out AbstractProjectileCypher<*>> get() = cyEntity.cypherHolder // FIXME this may lead to infinite loop

    /** no flag by default */
    override var enabledFlags = CypherFlags.fromFlags()

    protected var ccMap: MapOfCypherCounts? = null
    override fun ccMap(): MapOfCypherCounts? = ccMap

    protected val attributeMap: AttributeMap = HashMap<CypherAttribute, Double>()
    override fun attributeMap(): Map<CypherAttribute, Double> = attributeMap

    protected var hooks: HookContainer? = null
    override fun hooks(): HookContainer? = hooks

    override val hooksSharedData = HooksSharedData<CE>()
    override fun hooksSharedData(): HooksSharedData<CE> = hooksSharedData

    protected var trigger = TriggerType.NONE
    override fun triggerType(): TriggerType = trigger

    protected var payload: ShotStateChunk? = null
    override fun payload(): ShotStateChunk? = payload

    override fun getDirectionInitial(): Vec3 = _initDirection ?: Vec3.ZERO
    override fun getPositionInitial(): Vec3  = _initPosition ?: cyEntity.owner?.position() ?: Vec3.ZERO

    override fun getAttribute(attr: CypherAttribute): Double? = attributeMap[attr]
    override fun getAttribute(holer: Holder<CypherAttribute>): Double? = getAttribute(holer.value())
    override fun getAttributeOrDefault(attr: CypherAttribute): Double = attributeMap[attr] ?: cypher.getAttrBaseOrDefault(attr)
    override fun getAttributeOrDefault(holer: Holder<CypherAttribute>): Double = getAttributeOrDefault(holer.value())

    override fun getExisting(): Int = getAttributeOrDefault(CypherAttributes.EXISTING).toInt()
    override fun getBounce(): Int = getAttributeOrDefault(CypherAttributes.BOUNCE).toInt()
    override fun getGravityFactor(): Float = getAttributeOrDefault(CypherAttributes.GRAVITY_FACTOR).toFloat()
    override fun getSpeedFactor(): Float = 1f - getAttributeOrDefault(CypherAttributes.FRICTION_FACTOR).toFloat()
    override fun getEffectRadius(): Float = getAttributeOrDefault(CypherAttributes.EFFECT_RADIUS).toFloat()
    override fun getUnderwaterSpeedFactor() = 0.8f
    override fun getInWallSpeedFactor() = 0.5f
    override fun getBounceSpeedPenalty() = 0.95
    override fun getRotationSpeed(): Float = 0.25f

    override fun needCaptureSurrounding(): Boolean = false

    override fun getDamageSource(): DamageSource {
        return DamageSource(
            level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(CYPHER_DEFAULT),
            cyEntity,
            cyEntity.owner,
            cyEntity.position()
        )
    }

    protected var bounceCount = 0
    override val canBounce: Boolean get() = bounceCount < cyEntity.getBounce()
    override val bouncePoints = ArrayList<Vec3>()
    override val bouncedThisTick: Boolean get() = bouncePoints.isNotEmpty()

    protected var isInit: Boolean = false
    protected var lowSpeedTickCount = 0

    protected val collideWithBlocks: Boolean get() = noFlagsNone(CypherFlags.IGNORE_BLOCK, CypherFlags.PENETRATE_WORLD)
    protected val collideWithEntities: Boolean get() = noFlagsNone(CypherFlags.PENETRATE_WORLD)
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
        _cyEntity = cy as CE
        initDirection()
    }

    private var _initPosition: Vec3? = null // due to CyEntity init timing, remember direction data and init later
    private var _initDirection: Vec3? = null
    override fun initDirection(pair: PosDirePair) = run { _initDirection = pair.direction; _initPosition = pair.position }
    private fun initDirection() {
        if (level.isClientSide) return
        // when initDirection didn't call, vanilla setPos can handle it, with a direction ZERO
        if (_initPosition == null || _initDirection == null) return

        _initDirection = _initDirection ?: cyEntity.owner?.headLookAngle

        cyEntity.getPositionInitial().let { cyEntity.setPos(it) }
        cyEntity.getDirectionInitial().let {
            if (it == Vec3.ZERO) cyEntity.deltaMovement = Vec3.ZERO
            else {
                cyEntity.deltaMovement = it.normalize().scale(cyEntity.getAttributeOrDefault(CypherAttributes.SPEED))
                cyEntity.rotateTowardSpeed(1f)
            }
        }
        cyEntity.refreshDimensions()
        cyEntity.needsSync = true
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

    override fun trigger(type: TriggerType, releaseTo: PosDirePair) {
        if (trigger == TriggerType.NONE || type != trigger || payload == null) return
        payload!!.release(level, cyEntity, cyEntity.owner, releaseTo, null)
    }
    protected fun trigger(type: TriggerType, releasePoint: Vec3) {
        if (level.isClientSide) return
        when (type) {
            TriggerType.COLLISION ->
                trigger(type, PosDirePair(releasePoint, cyEntity.deltaMovement.reverse()))
            else ->
                trigger(type, PosDirePair(releasePoint, cyEntity.deltaMovement))
        }
    }


    override fun discardCypher(reason: DiscardReason) {
        if (level.isClientSide) return
        trigger(TriggerType.DEATH, cyEntity.position())
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
        if (count < 30) return
        if (cyEntity.getAttributeOrDefault(CypherAttributes.SPEED) > LOW_SPEED_THRESHOLD) {
            discardCypher(DiscardReason.LOW_SPEED)
        }
    }

    override fun doTick() {
        Profiler.get().push { "cypherEntityTick" }
//        if (level.isClientSide && cyEntity.tickCount and 4 == 4) {
//            println(cyEntity)
//        }

        captureSurroundings()
        if (cyEntity.firstTick) {
            onFirstTickBoth()
        }
        when (cyEntity.tickCount) {
            5 -> trigger(TriggerType.TIMER_5, cyEntity.position())
            10 -> trigger(TriggerType.TIMER_10, cyEntity.position())
            20 -> trigger(TriggerType.TIMER_20, cyEntity.position())
            40 -> trigger(TriggerType.TIMER_40, cyEntity.position())
            70 -> trigger(TriggerType.TIMER_70, cyEntity.position())
            200 -> trigger(TriggerType.TIMER_200, cyEntity.position())
        }

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
        /*
         * deltaMovement: the movement for the "next tick", client smooth animation relay on this
         * an AABB check is used everyTick every vanilla projectile, sounds outrageous, but is ok in performance
         * */
        tickBehaviorChangeBoth()

        cyEntity.rotateTowardSpeed(cyEntity.getRotationSpeed())
        applyFriction()
        applyGravity()
        tickMovementFinalizeBoth()

        if (cyEntity.deltaMovement.lengthSqr() <= LOW_SPEED_THRESHOLD_SQR) onLowSpeedBoth(lowSpeedTickCount++)
        else lowSpeedTickCount = 0

        loopHitAndBounce()
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
        if (!collideWithBlocks && !collideWithEntities) 1 // penetrate, do nothing
//        else if (speedSqr <= LOW_SPEED_THRESHOLD_SQR) 1 // too slow, do nothing
        else
        do {
            if (cyEntity.isRemoved) break
            hitSomething = false

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
                        // FIXME ConcurrentModificationException occurs when [pierce]
                        stepPosition.rayCastThen(destination, target.boundingBox, HIT_BB_INFLATION) { hitPoint, dir ->
                            whenHitDelegate(EntityHitResult(target, hitPoint), dir)
                        }
                    }
                } else {
                    // otherwise collide first
                    var nearest = Double.MAX_VALUE
                    var hitEntity: Entity? = null

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
                    onBounceBoth(bouncePoint)
                    bouncePoints.add(bouncePoint)

                    stepPosition = bouncePoint.add(bounceDirection.unitVec3.scale(1E-7)) // avoid "diving into blocks" bug
                    stepMovement = stepDestination0.subtract(destination).flipByDirection(
                        bounceDirection,
                        cyEntity.getBounceSpeedPenalty()
                    )
                }
            }

        } while (hitSomething && canBounce && loopTimes++ < 10)

//        if (cyEntity.firstTick) CypherNexus.LOGGER.info("${level.side()} removed ${cyEntity.isRemoved} \nstart ${cyEntity.position()}\nstep $stepPosition\ndelta $stepMovement")
        // finalize position
        cyEntity.setPos(stepPosition.add(stepMovement))
//        if (cyEntity.firstTick) CypherNexus.LOGGER.info("${level.side()} current ${cyEntity.position()}")

        if (loopTimes > 0) {
            cyEntity.deltaMovement = cyEntity.deltaMovement.toSameDire(stepMovement).scale(cyEntity.getBounceSpeedPenalty().pow(loopTimes))
        }

        Profiler.get().pop()
    }

    fun canHurtOwner(entity: CE): Boolean = entity.haveFlag(CypherFlags.HURT_OWNER) && !entity.firstTick
    override fun canHitTarget(target: Entity): Boolean {
        if (!target.canBeHitByProjectile()) {
            return false // vanilla logic, for item-entities
        }
        if (cyEntity.owner == null) return true
        if (!canHurtOwner(cyEntity) &&
            (cyEntity.owner == target || cyEntity.owner!!.isPassengerOfSameVehicle(target))) return false
        return true
    }

    // in case subclasses want to override whenHit
    // if implementing it here, any subclass of ICypherEntity can't call super.whenHit since its abstract
    // if implementing it inside ICypherEntity as a default function, there will be one more step to link to the function
    // cyEntity.whenHit -> Delegation.whenHit -> interface default
    protected fun whenHitDelegate(result: HitResult, direction: Direction) = cyEntity.whenHit(result, direction)
    override fun whenHit(result: HitResult, direction: Direction) {
        onHitBoth(result)
        if (level.isClientSide || result.type == Type.MISS) return
        trigger(TriggerType.COLLISION, result.location)

        if (result is EntityHitResult) {
            cyEntity.whenHitEntity(result, direction)

            if (!canBounce && noFlag(CypherFlags.PIERCE_ENTITY))
                discardCypher(DiscardReason.HIT_ENTITY)
        }
        else if (result is BlockHitResult) {
            cyEntity.whenHitBlock(result, direction)

            if (!canBounce)
                discardCypher(DiscardReason.HIT_BLOCK)
        }
    }

    override fun whenHitEntity(
        result: EntityHitResult,
        direction: Direction
    ) {
        val target = result.entity
        if (level.isClientSide) {
            if (noFlag(CypherFlags.SKIP_DAMAGE_CHECK))
            target.hurtClient(cyEntity.getDamageSource())
        }
        else {
            if (noFlag(CypherFlags.SKIP_DAMAGE_CHECK))
            cyEntity.exertDamage(level as ServerLevel, target)
        }
    }

    override fun whenHitBlock(
        result: BlockHitResult,
        direction: Direction
    ) {
        val blockPos = result.blockPos
        if (noFlag(CypherFlags.SILENT))
        this.level.gameEvent(GameEvent.PROJECTILE_LAND, blockPos, Context.of(cyEntity, level.getBlockState(blockPos)))
    }

    override fun canHomeTarget(target: Entity): Boolean {
        return canHitTarget(target)
                && target is LivingEntity
                && target !is Animal
                && target !is ICypherEntity
                && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(target)
                && !target.isInvisible
                && !target.isInvulnerable
                && target.isAlive
                && target != owner
                && target != cyEntity.owner
                && !target.`is`(EntityType.ARMOR_STAND)
    }

    override fun whileHomeTarget(target: Entity) {}
}