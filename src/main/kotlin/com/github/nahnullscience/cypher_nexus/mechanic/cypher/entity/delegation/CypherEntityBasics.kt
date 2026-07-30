package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation

import com.github.nahnullscience.cypher_nexus.init.data_driven.ModDamageTypes.CYPHER_DEFAULT
import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.init.mod.CypherHooks
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.DiscardReason
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation.ICypherEntity.Companion.CAPTURE_SIZE
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation.ICypherEntity.Companion.HIT_BB_INFLATION
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation.ICypherEntity.Companion.LOW_SPEED_THRESHOLD_SQR
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation.ICypherEntity.Companion.exertDamage
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookContainer
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HooksSharedData
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileNode
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.StateChunkPool
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.TriggerType
import com.github.nahnullscience.cypher_nexus.utility.*
import com.github.nahnullscience.cypher_nexus.utility.exception.CypherEntityException
import com.github.nahnullscience.cypher_nexus.utility.mod.AttributeFastMap
import com.github.nahnullscience.cypher_nexus.utility.mod.MapOfCypherCounts
import com.github.nahnullscience.cypher_nexus.utility.PosDirePair
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

    }

    private var _cyEntity: CE? = null
    protected val cyEntity: CE get() = _cyEntity ?:
    throw CypherEntityException("CypherEntityDelegation failed to initialize! make sure call #initEntity before it's adding to world!")
    override val cypherHolder: Holder<out AbstractProjectileCypher<*>> get() = cyEntity.cypherHolder // FIXME this may lead to infinite loop

    /** no flag by default */
    override var enabledFlags = CypherFlags.fromFlags()

    override var ccMap: MapOfCypherCounts? = null
    override val attributeMap = AttributeFastMap()
    override var hooks: HookContainer? = null
    override val hooksSharedData = HooksSharedData<CE>()

    override var triggerType = TriggerType.NONE
    override var payload: ShotStateChunk? = null

    override var hue: Int? = null
    override var hueFloatArray: FloatArray? = null

    protected val level get() = cyEntity.level()
    protected val random get() = cyEntity.random

    override fun getDirectionInitial(): Vec3 = _initDirection ?: Vec3.ZERO
    override fun getPositionInitial(): Vec3  = _initPosition ?: cyEntity.owner?.position() ?: Vec3.ZERO

    override fun getAttribute(attr: CypherAttribute): Double? = attributeMap[attr]
    override fun getAttribute(holer: Holder<CypherAttribute>): Double? = getAttribute(holer.value())
    override fun getAttributeOrDefault(attr: CypherAttribute) = attributeMap[attr] ?: cypher.getAttrBaseOrDefault(attr)
    override fun getAttributeOrDefault(holer: Holder<CypherAttribute>) = getAttributeOrDefault(holer.value())
    override fun getAttrBaseOrNull(holder: Holder<CypherAttribute>) = getAttrBaseOrNull(holder.value())
    override fun getAttrBaseOrNull(attr: CypherAttribute) = cypher.getAttrBaseOrNull(attr)

    override fun getExisting(): Int = getAttributeOrDefault(CypherAttributes.EXISTING).toInt()
    override fun getBounce(): Int = getAttributeOrDefault(CypherAttributes.BOUNCE).toInt()
    override fun getGravityFactor(): Double = getAttributeOrDefault(CypherAttributes.GRAVITY_FACTOR)
    override fun getSpeedFactor(): Double = 1f - getAttributeOrDefault(CypherAttributes.FRICTION_FACTOR)
    override fun getEffectRadius(): Float = getAttributeOrDefault(CypherAttributes.EFFECT_RADIUS).toFloat()
    override fun getUnderwaterSpeedFactor() = 0.8
    override fun getInWallSpeedFactor() = 0.5
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

    protected var capturedInitialSpeedSqr: Double = 0.0
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

    override fun initCypher(cypher: AbstractProjectileCypher<*>, shotState: ShotStateChunk, node: ProjectileNode?) {
        if (isInit) return
        attributeMap.initFromShotState(shotState, cypher)
        enabledFlags = shotState.enabledFlags or cypher.flags
        hooks = shotState.hooks
        ccMap = shotState.ccMap
        hue = shotState.dyeAccumulator.color
        hueFloatArray = shotState.dyeAccumulator.colorArray
        if (node != null) {
            triggerType = node.trigger
            payload = node.payload
        }

        isInit = true
    }

    @Suppress("UNCHECKED_CAST")
    override fun <E> initEntity (cy: E) where E : Entity, E : ICypherEntity {
        _cyEntity = cy as CE
        initDirection()
    }

    override fun initCypher(cypher: AbstractProjectileCypher<*>, ccMap: MapOfCypherCounts?) {
        if (ccMap == null) return
        val state = StateChunkPool.getOrCreateStateChunk(ccMap)
        initCypher(cypher, state, null)
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
        cyEntity.needsSync = true
    }

    protected open fun captureSurroundings() {
        if (cyEntity.tickCount == 1 || cyEntity.tickCount and 3 == 3) { // trigger on 1, 3 and then every 4 tick
            hooks?.get(CypherHooks.ENTITY_CAPTURE)?.let {
                var need = cyEntity.needCaptureSurrounding()
                if (!need) run {
                    hooks?.cumulateHooks(CypherHooks.ENTITY_CAPTURE, false) { _, hook, _, _ ->
                        need = hook.needCapture(level, cyEntity)
                        if (need) return@run
                        false
                    }
                }

                if (need) {
                    val entities = level.getEntities(
                        cyEntity,
                        cyEntity.boundingBox.inflate(CAPTURE_SIZE)
                    ) { entity -> cyEntity.canHitTarget(entity) && entity !is ICypherEntity }

                    for (entity in entities) {
                        delegateForEntityCaptured(entity)
                    }
                }
            }
        }
    }

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

    /**
     * [CypherHooks.BEFORE_DISCARD]
     * */
    protected open fun delegateBeforeDiscard(reason: DiscardReason) {
        cyEntity.beforeDiscard(reason)
        hooks?.playHooks(CypherHooks.BEFORE_DISCARD) { index, hook, count ->
            hook.beforeDiscard(index, count, level, cyEntity, reason)
        }
    }
    /**
     * [CypherHooks.HIT_ENTITY]
     * */
    protected open fun delegateOnHit(result: HitResult) {
        cyEntity.onHit(result)
        hooks?.playHooks(CypherHooks.HIT_ENTITY) { index, hook, count ->
            hook.onHit(index, count, level, cyEntity, result)
        }
    }
    /**
     * [CypherHooks.FIRST_TICK]
     * */
    protected open fun delegateOnFirstTick() {
        cyEntity.onFirstTick()
        hooks?.playHooks(CypherHooks.FIRST_TICK) { index, hook, count ->
            hook.onFirstTick(index, count, level, cyEntity)
        }
    }
    /**
     * [CypherHooks.TICK_BEHAVIOR]
     * change speed / attributes (here) -> finalize movement -> bounce & hit check
     * */
    protected open fun delegateOnTick() {
        cyEntity.onTick()
        hooks?.playHooks(CypherHooks.TICK_BEHAVIOR) { index, hook, count ->
            hook.onTick(index, count, level, cyEntity)
        }
    }
    /**
     * [CypherHooks.TICK_MOVEMENT_FINALIZE]
     * change speed / attributes -> finalize movement (here) -> bounce & hit check
     * */
    protected open fun delegateFinalizeTickMovement() {
        cyEntity.finalizeTickMovement()
        hooks?.playHooks(CypherHooks.TICK_MOVEMENT_FINALIZE) { index, hook, count ->
            hook.finalizeTickMovement(index, count, level, cyEntity)
        }
    }
    /**
     * [CypherHooks.ON_BOUNCE]
     * */
    protected open fun delegateOnBounce(point: Vec3) {
        cyEntity.onBounce(point)
        hooks?.playHooks(CypherHooks.ON_BOUNCE) { index, hook, count ->
            hook.onBounce(index, count, level, cyEntity, bounceCount, point)
        }
    }
    /**
     * [CypherHooks.ENTITY_CAPTURE]
     * */
    protected open fun delegateForEntityCaptured(captured: Entity) {
        cyEntity.forEntityCaptured(captured)
        hooks?.playHooks(CypherHooks.ENTITY_CAPTURE) { index, hook, count ->
            hook.forEntityCaptured(index, count, level, cyEntity, captured)
        }
    }
    /**
     * []
     * */
    protected open fun delegateOnLowSpeed(count: Int) {
        cyEntity.onLowSpeed(count)
    }

    override fun trigger(type: TriggerType, releaseTo: PosDirePair) {
        if (triggerType != TriggerType.NONE && type == triggerType) payload?.release(
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
                delegateBeforeDiscard(reason)
            }
        }
        level.broadcastEntityEvent(cyEntity, 3)
        cyEntity.discard()
    }

    override fun beforeDiscard(reason: DiscardReason) {}

    override fun onHit(result: HitResult) {}

    override fun onFirstTick() {}

    override fun onTick() {}

    override fun finalizeTickMovement() {}

    override fun onBounce(bouncePoint: Vec3) {}

    override fun forEntityCaptured(captured: Entity) {}

    override fun onLowSpeed(count: Int) {
        if (count < 7) return

        // this means the projectile is decelerated to low speed
        if (capturedInitialSpeedSqr > LOW_SPEED_THRESHOLD_SQR && noFlag(CypherFlags.MOTION_FOLLOWS_OWNER)) {
            discardCypher(DiscardReason.LOW_SPEED)
        }
    }

//    override fun onDealDamage(damage: Double) {}

    override fun doTick() {
        Profiler.get().push { "cypherEntityTick" }

        if (cyEntity.tickCount == 1) {
            delegateOnFirstTick()
        }

        captureSurroundings()

        when (cyEntity.tickCount) {
            5 -> trigger(TriggerType.TIMER_5, cyEntity.position())
            10 -> trigger(TriggerType.TIMER_10, cyEntity.position())
            20 -> trigger(TriggerType.TIMER_20, cyEntity.position())
            40 -> trigger(TriggerType.TIMER_40, cyEntity.position())
            70 -> trigger(TriggerType.TIMER_70, cyEntity.position())
            200 -> trigger(TriggerType.TIMER_200, cyEntity.position())
        }

        if (cyEntity.tickCount == 3) {
            capturedInitialSpeedSqr = cyEntity.deltaMovement.lengthSqr()
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
        delegateOnTick()

        cyEntity.rotateTowardSpeed(cyEntity.getRotationSpeed())
        applyFriction()
        applyGravity()
        delegateFinalizeTickMovement()

        if (cyEntity.deltaMovement.lengthSqr() <= LOW_SPEED_THRESHOLD_SQR) delegateOnLowSpeed(lowSpeedTickCount++)
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
                if (haveFlag(CypherFlags.PIERCE_ENTITY)) {
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
                    delegateOnBounce(bouncePoint)
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

    fun canHurtOwner(entity: CE): Boolean = entity.haveFlag(CypherFlags.HURT_OWNER) && entity.tickCount != 1
    override fun canHitTarget(target: Entity): Boolean {
        if (!target.canBeHitByProjectile()) {
            return false // vanilla logic, for item-entities
        }
        if (cyEntity.owner == null) return true
        if (!canHurtOwner(cyEntity) &&
            (cyEntity.owner == target || cyEntity.owner!!.isPassengerOfSameVehicle(target))) return false
        return true
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
        delegateOnHit(result)
        if (level.isClientSide || result.type == Type.MISS) return
        trigger(TriggerType.COLLISION, result.location)

        if (result is EntityHitResult) {
            whenHitEntityDelegate(result, direction)

            if (!canBounce && noFlag(CypherFlags.PIERCE_ENTITY))
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
            if (noFlag(CypherFlags.SKIP_DAMAGE_CHECK))
                target.hurtClient(cyEntity.getDamageSource())
        }
        else {
            if (noFlag(CypherFlags.SKIP_DAMAGE_CHECK))
                cyEntity.exertDamage(level as ServerLevel, target)
        }
    }

    protected open fun whenHitBlockDelegate(
        result: BlockHitResult,
        direction: Direction
    ) {
        cyEntity.whenHitBlock(result, direction)
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