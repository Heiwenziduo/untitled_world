package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.init.mod.CypherBehaviorHooks
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookContainer
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileNode
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileStateChunk
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.TriggerType
import com.github.nahnullscience.cypher_nexus.utility.RayCastUtility
import com.github.nahnullscience.cypher_nexus.utility.VectorUtility
import com.github.nahnullscience.cypher_nexus.utility.i.IFlaggable
import com.github.nahnullscience.cypher_nexus.utility.mod.CypherUtility
import com.github.nahnullscience.cypher_nexus.utility.mod.PosDirePair
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.Holder
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Explosion
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import java.util.HashMap
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

abstract class AbstractCypherProjectile(
    entityType: EntityType<out AbstractCypherProjectile>,
    level: Level
) : Projectile(entityType, level), IFlaggable {
    companion object {
        const val CLIP_MARGIN = 0.2f

        /** generate projectile with attributes initialized */
        fun <T : AbstractCypherProjectile> create(
            entityType: EntityType<T>,
            level: Level,
            invoker: Entity?,
            direction: Vec3? = null,
            shootState: ProjectileStateChunk,
            node: ProjectileNode,
            stateHooks: HookContainer?
        ) : T {
            if (level.isClientSide) throw IllegalStateException("Try to create projectile [$entityType] on client side.")
            val proj = entityType.create(level) ?: throw IllegalStateException("Failed to create projectile [$entityType].")
            proj.initialize(invoker, direction, shootState, node, stateHooks)
            return proj
        }


//        val CYPHER: EntityDataAccessor<AbstractCypher> = SynchedEntityData.defineId(
//            AbstractCypherProjectile::class.java,
//            ModDataSerializers.CYPHER_DATA.get())
//        val CYPHER_LIST: EntityDataAccessor<List<AbstractCypher>> = SynchedEntityData.defineId(
//            AbstractCypherProjectile::class.java,
//            ModDataSerializers.CYPHER_LIST_DATA.get())
        val FLAG: EntityDataAccessor<Int> = SynchedEntityData.defineId(
            AbstractCypherProjectile::class.java,
            EntityDataSerializers.INT)
        val EXISTING: EntityDataAccessor<Int> = SynchedEntityData.defineId(
            AbstractCypherProjectile::class.java,
            EntityDataSerializers.INT)
        val BOUNCE: EntityDataAccessor<Int> = SynchedEntityData.defineId(
            AbstractCypherProjectile::class.java,
            EntityDataSerializers.INT)
        val GRAVITY: EntityDataAccessor<Float> = SynchedEntityData.defineId(
            AbstractCypherProjectile::class.java,
            EntityDataSerializers.FLOAT)
        val SPEED_FACTOR: EntityDataAccessor<Float> = SynchedEntityData.defineId(
            AbstractCypherProjectile::class.java,
            EntityDataSerializers.FLOAT)
    }
    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
        builder.define(FLAG, 0)
        builder.define(EXISTING, 1)
        builder.define(BOUNCE, 0)
        builder.define(GRAVITY, 0f)
        builder.define(SPEED_FACTOR, 0.99f)
    }
    override fun onSyncedDataUpdated(key: EntityDataAccessor<*>) {
        super.onSyncedDataUpdated(key)
    }

    override fun onAddedToLevel() {
        super.onAddedToLevel()
    }

    // ==================================================================================================================
    // ==================================================================================================================
    abstract val cypher: AbstractProjectileCypher
    private var _existing: Int = 0
    val existing get() = _existing

    /** a flag is basically a bundle of booleans */
    override var enabledFlags: Int
        get() = entityData.get(FLAG)
        set(value) = entityData.set(FLAG, value)

//    var existing: Int
//        get() = entityData.get(EXISTING)
//        set(value) = entityData.set(EXISTING, value)

    var bounce: Int
        get() = entityData.get(BOUNCE)
        private set(value) = entityData.set(BOUNCE, value)
    var gravity: Float
        get() = entityData.get(GRAVITY)
        private set(value) = entityData.set(GRAVITY, value)
    var speedFactor: Float
        get() = entityData.get(SPEED_FACTOR)
        private set(value) = entityData.set(SPEED_FACTOR, value)


//    /** the direct entity create the projectile, invoker could be another projectile if trigger */
//    private var _invoker: Entity? = null
//    val invoker
//        get() = _invoker
    /** position where the cypher was invoked initially, before any hook modify */
//    private var _invokedPosition: Vec3? = null
//    /** position where the cypher was invoked initially, before any hook modify */
//    var invokedPosition
//        get() = _invokedPosition
//        set(value) = run { _invokedPosition = value }
    var moveDirection: Vec3 = Vec3.ZERO

    // should be immutable after initialization
    private val _attributeMap = HashMap<CypherAttribute, Double>()
    /** store bounce points triggered in one tick */
    protected val bouncePoints = mutableListOf<Vec3>()
    protected val bounceTick get() = bouncePoints.isNotEmpty()

    private var hooks: HookContainer? = null
    private var _trigger = TriggerType.NONE
    private var _payload: ProjectileStateChunk? = null
    val payload: ProjectileStateChunk? get() = _payload

    // ==================================================================================================================
    // ==================================================================================================================
//    constructor(entityType: EntityType<out AbstractCypherProjectile>, level: Level) : super(entityType, level)
//    /** create a plain projectile of the given cypher */
//    private constructor(entityType: EntityType<out AbstractCypherProjectile>, level: Level, invoker: Entity?, direction: Vec3? = null
//    ) : this(entityType, level) {
//        owner = invoker
//        enableFlag(cypher.flag)
//        setHooks(null)
//        setDirection(direction)
//    }
//
//    /**  */
//    constructor(
//        entityType: EntityType<out AbstractCypherProjectile>,
//        level: Level,
//        invoker: Entity?,
//        direction: Vec3? = null,
//        shootState: ProjectileStateChunk,
//        node: ProjectileNode,
//        parentHooks: HookContainer?
//    ) : this(entityType, level) {
//        owner = invoker
//        enabledFlags = shootState.enabledFlags or cypher.flag
//        _payload = node.payload
//        _trigger = node.trigger
//        setHooks(parentHooks)
//
//        initAttributes(shootState)
//        setDirection(direction)
//
//        printDebugMsg()
//    }
    private var isInitialized = false
    private fun initialize(
        invoker: Entity?,
        direction: Vec3? = null,
        shootState: ProjectileStateChunk,
        node: ProjectileNode,
        stateHooks: HookContainer?
    ) {
        if (isInitialized) CypherNexus.LOGGER.debug("{} is already initialized", this)
        owner = invoker
        enabledFlags = shootState.enabledFlags or cypher.flag
        _payload = node.payload
        _trigger = node.trigger

        setHooks(stateHooks)
        initAttributes(shootState)
        setDirection(direction)

        printDebugMsg()
        isInitialized = true
    }

    // ==================================================================================================================
    // ==================================================================================================================
    protected fun initAttributes(shootState: ProjectileStateChunk) {
        shootState.computedOperationMap.forEach { (attr, opMap) ->
            if (!attr.isProjectileAttribute) return@forEach
            if (haveFlag(CypherFlags.CONSTANT_EXISTING) && CypherAttributes.EXISTING.`is`(attr.resource)) return@forEach

            _attributeMap.compute(attr) { a, v ->
                val def = cypher.getAttrBaseOrDefault(attr)
                val final = CypherUtility.attributeCalculator(opMap, def)
                attr.restrictRange(final)
            }
        }

        _existing = getAttrOrProjDefault(CypherAttributes.EXISTING).toInt()
        bounce = getAttrOrProjDefault(CypherAttributes.BOUNCE).toInt()
        gravity = getAttrOrProjDefault(CypherAttributes.GRAVITY_FACTOR).toFloat()
        speedFactor = 1f - getAttrOrProjDefault(CypherAttributes.FRICTION_FACTOR).toFloat()
    }

    protected fun setDirection(direction: Vec3? = null) {
        moveDirection = direction?.normalize() ?: owner?.lookAngle?.normalize() ?: moveDirection
        if (moveDirection != Vec3.ZERO){
            deltaMovement = moveDirection.scale(getAttrOrProjDefault(CypherAttributes.SPEED))
            // FIXME inertia behavior seems strange
        } else {
            deltaMovement = Vec3.ZERO
        }
    }
    fun setDirection(pair: PosDirePair) {
        setPos(pair.position)
        setDirection(pair.direction)
    }
//    fun setPosInitial(vec3: Vec3) = run { _invokedPosition = vec3 }

    protected fun setHooks(parent: HookContainer?) {
        hooks = HookContainer(Optional.ofNullable(parent))
        hooks?.add(cypher)
    }


    // ==================================================================================================================
    // ==================================================================================================================
    override fun tick() {
        // called on both server side and client side
        if (firstTick) { // start from tickCount == 1
            // FIXME entity desync "positon-flash" almost always happen at around first time they sync
            // FIXME aiming deviation at high speed
            hooks?.playHooks(CypherBehaviorHooks.FIRST_TICK_BOTH)
            { h, i -> h.firstTickBoth(level(), this, i) }

            if (level().isClientSide) {
                println("firstTickCheckOnClient: $cypher") // attrs are synced from the start
            }

            // if (!level().isClientSide) deltaMovement = Vec3.ZERO // deltaMovement will auto-sync to client, but not immediately
        }

//        updateInWaterStateAndDoFluidPushing()
//        updateFluidOnEyes()
//        updateSwimming()
        super.tick() // TODO: prune default tick

        // hookContainer.get(TICK_BEHAVIOR).forEach { h, i -> h.tickBehaviorBoth(level(), this, i) }
        hooks?.playHooks(CypherBehaviorHooks.TICK_BEHAVIOR_BOTH)
        { h, i -> h.tickBehaviorBoth(level(), this, i) }
        projectileTick()
        modifierTick()

        if (tickCount == 20) trigger(TriggerType.TIMER_20)
        if (_existing < 0 || _existing == tickCount) {
            // here's a trick, if player make existing-time exactly equal to 0, projectile will last till the game quit
            discardCy(DiscardReason.EXPIRE)
        }
    }


    protected open fun modifierTick() {}
    /**
     * check hit-result and set delta-movement here
     * */
    protected open fun projectileTick() {
        /*
         * deltaMovement: the movement for the "next tick", client smooth animation relay on this
         * // an AABB check is used everyTick every vanilla projectile, sounds outrageous, but is ok in performance
         * */
        val hitResult = RayCastUtility.getProjectileHitResult(position(), this, ::canHitEntity, deltaMovement, level(), CLIP_MARGIN)
        bouncePoints.clear()
        val (lastBouncePoint, lastDeltaMove) = bounceLoop(hitResult)
        if (bounceTick) deltaMovement = VectorUtility.toSameDire(deltaMovement, lastDeltaMove)

//        run collideCheck@ {
//            val fluidCheck = ClipContext.Fluid.NONE // bounce when touching water surface?
//            val blockHit = level().clip(ClipContext(position(), deltaMovement, ClipContext.Block.COLLIDER, fluidCheck, this))
//        }

        checkInsideBlocks() // trigger #onInsideBlock

        updateRotation()

        applySpeedChange()
        applyGravity()

        // #move do exactly the same with #setPos when dealing with "noPhysics"
        if (bounceTick) setPos(lastBouncePoint.add(lastDeltaMove))
        else setPos(position().add(deltaMovement))
    }


    // ==================================================================================================================
    // ==================================================================================================================

    override fun applyGravity() {
        deltaMovement = deltaMovement.add(0.0, -(gravity).toDouble(), 0.0)
    }
    protected fun applySpeedChange() {
        val f: Float = if (isInWater) 0.8f * speedFactor else speedFactor
        deltaMovement = deltaMovement.scale(f.toDouble())
    }

    // ==================================================================================================================
    // ==================================================================================================================
    /**
     * handle bounce movement logic and trigger #onHit.
     * @return a pair of lastHitPoint and deltaMove for the last leg, current #position and #deltaMovement if no bounce.
     * */
    private fun bounceLoop(hitResult: HitResult): Pair<Vec3, Vec3> {
        val defaultReturn = Pair(position(), deltaMovement)
        if (hitResult.type == HitResult.Type.MISS) return defaultReturn
        var hitResultStep = hitResult
        var startPosStep = position()
        var deltaMoveStep = deltaMovement

        do {
            // EventHooks.onProjectileImpact(this, hitResultStep), maybe get a result from broadcast
//            if (!level().isClientSide) println("loop$bounce: \n$hitResultStep\n$startPosStep\n$deltaMoveStep")

            // FIXME image a situation that one proj with bounce can pierce block but can not pierce entity, it should bounce back when an entity stand behind a wall
            onHit(hitResultStep) // or hitTargetOrDeflectSelf(hitResult)
            val canPierce = hitResultStep is BlockHitResult && haveFlag(CypherFlags.PIERCE_BLOCK)
                    || hitResultStep is EntityHitResult && haveFlag(CypherFlags.PIERCE_ENTITY)
            if (bounce <= 0 || canPierce) break

            val targetBox = when(hitResultStep) {
                is EntityHitResult -> hitResultStep.entity.boundingBox.inflate(CLIP_MARGIN.toDouble())
                is BlockHitResult -> AABB(hitResultStep.blockPos)
                else -> AABB(BlockPos(VectorUtility.toVec3i(hitResultStep.location)))
            }
            val hitPoint = targetBox.clip(startPosStep, startPosStep.add(deltaMoveStep)).getOrNull()

//            if (!level().isClientSide) println("hitPoint $hitPoint \naabb $targetBox")

            val direction = VectorUtility.getDireFromHit(hitPoint, targetBox)
            if (hitPoint == null || direction == null) { // this block should not be reached
                if (!level().isClientSide) CypherNexus.LOGGER.error("hitPoint == null || direction == null\n$direction")
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
            bounce--
            bouncePoints.add(hitPoint)

            // handle next bounce
            hitResultStep = RayCastUtility.getProjectileHitResult(startPosStep, this, ::canHitEntity, deltaMoveStep, level(), CLIP_MARGIN)

        } while (hitResultStep.type != HitResult.Type.MISS)

        return Pair(startPosStep, deltaMoveStep)
    }


    // ==================================================================================================================
    // ==================================================================================================================
    override fun canHitEntity(target: Entity): Boolean {
        if (!target.canBeHitByProjectile()) {
            return false // vanilla logic, for item-entities
        }
        // if (haveFlag(CypherFlags.NO_DAMAGE)) return false
        if (owner == target && notHaveFlag(CypherFlags.HURT_OWNER)) return false
        // maybe hook
        return true
    }
    override fun onHit(result: HitResult) {
        super.onHit(result) // distribute hitResult
        if (level().isClientSide) return
        trigger(TriggerType.COLLISION)

        hooks?.playHooks(CypherBehaviorHooks.HIT_ENTITY_SERVER)
        { h, i -> h.onHitServer(level(), this, i, result) }

        val canPierce =
            result is BlockHitResult && haveFlag(CypherFlags.PIERCE_BLOCK) ||
            result is EntityHitResult && haveFlag(CypherFlags.PIERCE_ENTITY)
        if (!canPierce && bounce <= 0) {
            level().broadcastEntityEvent(this, 3) // combine with #handleEntityEvent
            discardCy(if (result.type == HitResult.Type.BLOCK) DiscardReason.HIT_BLOCK else DiscardReason.HIT_ENTITY)
        }

    }
    override fun onHitEntity(result: EntityHitResult) {
        super.onHitEntity(result)
        val entity = result.entity
        if (notHaveFlag(CypherFlags.NO_DAMAGE)) {
            val damage = getAttrOrProjDefault(CypherAttributes.DAMAGE)
            entity.hurt(damageSources().thrown(this, owner), damage.toFloat())
        }
    }
    override fun onHitBlock(result: BlockHitResult) {
        super.onHitBlock(result)
    }

    // ==================================================================================================================
    // ==================================================================================================================
    /** ProjectileStateBlock#release */
    private fun releasePayload(posDire: PosDirePair) = _payload?.release(level(), this, owner, posDire)
    fun trigger(type: TriggerType) {
        // TODO
        if (type == _trigger) releasePayload(PosDirePair(position(), deltaMovement.reverse()))
    }

    // ==================================================================================================================
    // ==================================================================================================================
    override fun handleEntityEvent(id: Byte) {
        // trigger on client
        super.handleEntityEvent(id)
        if (id.toInt() == 3) {
//            cypher.visualEffectOnHit(level(), this)
        }
    }

    override fun shouldRender(x: Double, y: Double, z: Double): Boolean = super.shouldRender(x, y, z)
    override fun shouldRenderAtSqrDistance(distance: Double): Boolean {
        // default distance based on AABB size, this is vital for very small entities
        // getViewScale()
        return distance <= 4096.0
    }

    override fun displayFireAnimation() = haveFlag(CypherFlags.WITH_FIRE)


    // ==================================================================================================================
    // ==================================================================================================================
    fun getAttribute(attr: CypherAttribute): Double? = _attributeMap.get(attr)
    fun getAttribute(holer: Holder<CypherAttribute>): Double? = getAttribute(holer.value())
    fun getAttrOrProjDefault(attr: CypherAttribute): Double = _attributeMap[attr] ?: cypher.getAttrBaseOrDefault(attr)
    /** computedOperationMap > projectileCypher-base > attr#default */
    fun getAttrOrProjDefault(holer: Holder<CypherAttribute>): Double = getAttrOrProjDefault(holer.value())


    // ==================================================================================================================
    // ==================================================================================================================
    override fun readAdditionalSaveData(compound: CompoundTag) = Unit
    override fun addAdditionalSaveData(compound: CompoundTag) = Unit
    override fun getPickResult(): ItemStack? = null // null by default, this is the creative mod middle button pick result
    override fun isPickable() = false // false by default, entirely disable the picking activity
    /**
     * since the projectile can't exist without a related cypher,
     * the #deltaMovement initialization will be done automatically, call #shoot is not necessary
     * */
    override fun shoot(x: Double, y: Double, z: Double, velocity: Float, inaccuracy: Float) = Unit // do nothing, don't call
    override fun handlePortal() {
        // TODO
        super.handlePortal()
    }
    // a public method, to get a HitResult by checking if there is any block or entity in the direction of #getViewVector
    // ray cast
//    override fun pick(hitDistance: Double, partialTicks: Float, hitFluids: Boolean): HitResult {
//        return super.pick(hitDistance, partialTicks, hitFluids)
//    }
    override fun hurt(source: DamageSource, amount: Float) = false
    override fun ignoreExplosion(explosion: Explosion) = true // this prevents projectile getting kinetic energy from explosion




    protected fun discardCy(reason: DiscardReason) {
        if (level().isClientSide) return
        trigger(TriggerType.DEATH)
        when(reason){
            DiscardReason.ERASE -> {

            }
            else -> {
                if (reason == DiscardReason.EXPIRE) {
//                    if (level().isClientSide) cypher.visualEffectOnExpire(level(), this)
                }
                hooks?.playHooks(CypherBehaviorHooks.BEFORE_DISCARD_BOTH)
                { h, i -> h.beforeDiscardBoth(level(), this, i, reason) }
            }
        }
        discard()
    }

    // ==================================================================================================================
    // ==================================================================================================================
    private fun printDebugMsg() {
        CypherNexus.LOGGER.debug("create projectile {}: {}", this, cypher)
        CypherFlags.Companion.printFlag(enabledFlags)

        // modified AttrMap
        _attributeMap.forEach { a, v ->
            println("$a: $v")
        }
        if (_attributeMap.isEmpty()) println("projectile $cypher has no modified attributes")
    }



    // ==================================================================================================================
    // ==================================================================================================================
    // check Entity.RemovalReason for more info
    // here only for cypher-projectile usage
    enum class DiscardReason {
        /** reach its time limit (e.g. naturally expire) */
        EXPIRE,
        /** through a collapse with entity */
        HIT_ENTITY,
        /** through a collapse with block */
        HIT_BLOCK,
        /**  */
        TRANSFORMED,

        /** by some special reason */
        ERASE,
    }
}