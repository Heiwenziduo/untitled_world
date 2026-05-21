package com.github.nahnullscience.cypher_nexus.content.entity

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.ModDataSerializer
import com.github.nahnullscience.cypher_nexus.init.ModEntities
import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.init.mod.CypherBehaviorHookRegistry
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.EmptyCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookContainer
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookModule
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileStateBlock
import com.github.nahnullscience.cypher_nexus.utility.RayCastUtility
import com.github.nahnullscience.cypher_nexus.utility.VectorUtility
import com.github.nahnullscience.cypher_nexus.utility.i.IFlaggable
import com.github.nahnullscience.cypher_nexus.utility.mod.CypherUtility
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.Holder
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.phys.*
import kotlin.jvm.optionals.getOrNull


open class CypherProjectile(entityType: EntityType<out Projectile>, level: Level) :
    Projectile(entityType, level), IFlaggable
{
    // var cypher: AbstractProjectileCypher = EmptyCypher
    // private var invokeList: List<AbstractCypher> = listOf()
    private var _cypher: AbstractProjectileCypher
        get() = entityData.get(CYPHER) as AbstractProjectileCypher
        set(value) = entityData.set(CYPHER, value)
    /** only NonProjectile cyphers, check CypherModifierHelper#preInvoke */
    private var _invokeList: List<AbstractCypher>
        get() = entityData.get(CYPHER_LIST)
        set(value) = entityData.set(CYPHER_LIST, value)

    val cypher
        get() = _cypher
    /** only NonProjectile cyphers, check CypherModifierHelper#preInvoke */
    val invokeList
        get() = _invokeList

    // private var _moveDireCache: Vec3? = null
    var moveDirection: Vec3 = Vec3.ZERO
    /** a flag is basically a bundle of booleans */
    override var enabledFlags: Int
        get() = entityData.get(FLAG)
        set(value) = entityData.set(FLAG, value)

    var existing: Int
        get() = entityData.get(EXISTING)
        set(value) = entityData.set(EXISTING, value)
    var bounce: Int
        get() = entityData.get(BOUNCE)
        set(value) = entityData.set(BOUNCE, value)
    var gravity: Float
        get() = entityData.get(GRAVITY)
        set(value) = entityData.set(GRAVITY, value)
    var speedFactor: Float
        get() = entityData.get(SPEED_FACTOR)
        set(value) = entityData.set(SPEED_FACTOR, value)


    // should be immutable after initialization
    private val _attributeMap = HashMap<CypherAttribute, Double>()
    /** store bounce points triggered in one tick */
    protected val bouncePoints = mutableListOf<Vec3>()
    protected val bounceTick
        get() = bouncePoints.isNotEmpty()
    val clipMargin = 0.2f
    val projHookContainer = HookContainer(HookModule.HookType.PROJECTILE)

    private constructor(level: Level, cypher0: AbstractProjectileCypher, invoker: Entity?, direction: Vec3? = null) : this(
        ModEntities.CYPHER_PROJECTILE.get(), level) {
        owner = invoker
        _cypher = cypher0
        enableFlag(_cypher.flag)
        syncHooks(_invokeList, _cypher)
        moveDirection = direction?: invoker?.lookAngle?.normalize()?: moveDirection
        if (moveDirection != Vec3.ZERO){
            deltaMovement = moveDirection.scale(getAttrOrProjDefault(CypherAttributes.SPEED))
        } else {
            deltaMovement = Vec3.ZERO
        }
    }

    constructor(
        level: Level, invoker: LivingEntity?, cypher0: AbstractProjectileCypher, direction: Vec3? = null,
        shootState: ProjectileStateBlock, payload: ProjectileStateBlock?
        ) : this(ModEntities.CYPHER_PROJECTILE.get(), level)
    {
        owner = invoker
        _cypher = cypher0
        enabledFlags = shootState.enabledFlags
        enableFlag(_cypher.flag)
        // TODO
//        _invokeList = invokeList0
//        syncHooks(_invokeList, _cypher)

        shootState.computedOperationMap.forEach { (attr, opMap) ->
            if (!attr.isProjectileAttribute) return@forEach
            _attributeMap.compute(attr) { a, v ->
                val def = _cypher.getAttrBaseOrDefault(attr)
                val final = CypherUtility.attributeCalculator(opMap, def)
                attr.restrictRange(final)
            }
        }

        existing = getAttrOrProjDefault(CypherAttributes.EXISTING).toInt()
        bounce = getAttrOrProjDefault(CypherAttributes.BOUNCE).toInt()
        gravity = getAttrOrProjDefault(CypherAttributes.GRAVITY_FACTOR).toFloat()
        speedFactor = 1f - getAttrOrProjDefault(CypherAttributes.FRICTION_FACTOR).toFloat()

        moveDirection = direction?: invoker?.lookAngle?.normalize()?: moveDirection
        if (moveDirection != Vec3.ZERO){
            deltaMovement = moveDirection.scale(getAttrOrProjDefault(CypherAttributes.SPEED))
            // FIXME inertia behavior seems strange
        } else {
            deltaMovement = Vec3.ZERO
        }

        CypherNexus.LOGGER.debug("create projectile {}", _cypher)
        CypherFlags.Companion.printFlag(enabledFlags)
        printModifiedAttrMap()
    }


    companion object {
        /** generate projectile with raw attributes */
        fun from(level: Level, cypher0: AbstractProjectileCypher, invoker: Entity?, direction: Vec3? = Vec3.ZERO) : CypherProjectile {
            return CypherProjectile(level, cypher0, invoker, direction)
        }


        val CYPHER: EntityDataAccessor<AbstractCypher> = SynchedEntityData.defineId(
            CypherProjectile::class.java,
            ModDataSerializer.CYPHER_DATA.get())
        val CYPHER_LIST: EntityDataAccessor<List<AbstractCypher>> = SynchedEntityData.defineId(
            CypherProjectile::class.java,
            ModDataSerializer.CYPHER_LIST_DATA.get())
        val FLAG: EntityDataAccessor<Int> = SynchedEntityData.defineId(
            CypherProjectile::class.java,
            EntityDataSerializers.INT)
        val EXISTING: EntityDataAccessor<Int> = SynchedEntityData.defineId(
            CypherProjectile::class.java,
            EntityDataSerializers.INT)
        val BOUNCE: EntityDataAccessor<Int> = SynchedEntityData.defineId(
            CypherProjectile::class.java,
            EntityDataSerializers.INT)
        val GRAVITY: EntityDataAccessor<Float> = SynchedEntityData.defineId(
            CypherProjectile::class.java,
            EntityDataSerializers.FLOAT)
        val SPEED_FACTOR: EntityDataAccessor<Float> = SynchedEntityData.defineId(
            CypherProjectile::class.java,
            EntityDataSerializers.FLOAT)
    }
    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
        builder.define(CYPHER, EmptyCypher)
        builder.define(CYPHER_LIST, listOf())
        builder.define(FLAG, 0)
        builder.define(EXISTING, 1)
        builder.define(BOUNCE, 0)
        builder.define(GRAVITY, 0f)
        builder.define(SPEED_FACTOR, 0.99f)
    }

    private fun syncHooks(modifiers: List<AbstractCypher>, thiz: AbstractCypher? = null) {
        for (c in modifiers) {
            projHookContainer.add(c)
        }
        if (thiz != null) {
            projHookContainer.add(thiz)
        }
    }

    // ==================================================================================================================
    // ==================================================================================================================
    override fun tick() {
        // called on both server side and client side
        if (firstTick) { // start from tickCount == 1
            projHookContainer.playHooks(CypherBehaviorHookRegistry.FIRST_TICK)
            { h, i -> h.firstTickBoth(level(), this, i) }

            if (level().isClientSide) {
                println("firstTickCheckOnClient: _cypher, _invokeList\n$_cypher\n$_invokeList") // attrs are synced from the start
            }
        }

//        updateInWaterStateAndDoFluidPushing()
//        updateFluidOnEyes()
//        updateSwimming()
        super.tick() // TODO: prune default tick

        // hookContainer.get(TICK_BEHAVIOR).forEach { h, i -> h.tickBehaviorBoth(level(), this, i) }
        projHookContainer.playHooks(CypherBehaviorHookRegistry.TICK_BEHAVIOR)
        { h, i -> h.tickBehaviorBoth(level(), this, i) }
        projectileTick()
        modifierTick()
        if (existing == tickCount || haveFlag(CypherFlags.LIMITED_EXISTING)) {
            // here's a trick, if player make existing-time less or equal to 0, projectile will last till the game quit
            discardCy(DiscardReason.EXPIRE)
        }
    }


    protected fun modifierTick() {}
    /**
     * check hit-result and set delta-movement here
     * */
    protected fun projectileTick() {
        /*
         * deltaMovement: the movement for the "next tick", client smooth animation relay on this
         * // an AABB check is used everyTick every vanilla projectile, sounds outrageous, but is ok in performance
         * */
        val hitResult = RayCastUtility.getProjectileHitResult(position(), this, ::canHitEntity, deltaMovement, level(), clipMargin)
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
    /**
     * since the projectile can't exist without a related cypher,
     * the #deltaMovement initialization will be done automatically, call #shoot is not necessary
     * */
    override fun shoot(x: Double, y: Double, z: Double, velocity: Float, inaccuracy: Float) { } // do nothing, don't call

    override fun applyGravity() {
        deltaMovement = deltaMovement.add(0.0, -(gravity).toDouble(), 0.0)
    }
    protected fun applySpeedChange() {
        val f: Float = if (isInWater) 0.8f * speedFactor else speedFactor
        deltaMovement = deltaMovement.scale(f.toDouble())
    }

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
            if (!level().isClientSide) println("loop$bounce: \n$hitResultStep\n$startPosStep\n$deltaMoveStep")

            // FIXME image a situation that one proj with bounce can pierce block but can not pierce entity, it should bounce back when an entity stand behind a wall
            onHit(hitResultStep) // or hitTargetOrDeflectSelf(hitResult)
            val canPierce = hitResultStep is BlockHitResult && haveFlag(CypherFlags.PIERCE_BLOCK)
                    || hitResultStep is EntityHitResult && haveFlag(CypherFlags.PIERCE_ENTITY)
            if (bounce <= 0 || canPierce) break

            val targetBox = when(hitResultStep) {
                is EntityHitResult -> hitResultStep.entity.boundingBox.inflate(clipMargin.toDouble())
                is BlockHitResult -> AABB(hitResultStep.blockPos)
                else -> AABB(BlockPos(VectorUtility.toVec3i(hitResultStep.location)))
            }
            val hitPoint = targetBox.clip(startPosStep, startPosStep.add(deltaMoveStep)).getOrNull()

            if (!level().isClientSide) println("hitPoint $hitPoint \naabb $targetBox")

            val direction = VectorUtility.getDireFromHit(hitPoint, targetBox)
            if (hitPoint == null || direction == null) { // this block should not be reached
                if (!level().isClientSide) CypherNexus.LOGGER.fatal("hitPoint == null || direction == null\n$direction")
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
            hitResultStep = RayCastUtility.getProjectileHitResult(startPosStep, this, ::canHitEntity, deltaMoveStep, level(), clipMargin)

        } while (hitResultStep.type != HitResult.Type.MISS)

        return Pair(startPosStep, deltaMoveStep)
    }

    override fun onHit(result: HitResult) {
        // distribute hitResult
        super.onHit(result)


        if (level().isClientSide) return
        val canPierce =
            result is BlockHitResult && haveFlag(CypherFlags.PIERCE_BLOCK) ||
            result is EntityHitResult && haveFlag(CypherFlags.PIERCE_ENTITY)
        if (!canPierce && bounce <= 0) {
            level().broadcastEntityEvent(this, 3) // combine with #handleEntityEvent
            discardCy(if (result.type == HitResult.Type.BLOCK) DiscardReason.HIT_BLOCK else DiscardReason.HIT_ENTITY)
        }
    }
    override fun canHitEntity(target: Entity): Boolean {
        if (!target.canBeHitByProjectile()) {
             return false // vanilla logic, for item-entities
        }
        // if (haveFlag(CypherFlags.NO_DAMAGE)) return false
        if (owner == target && notHaveFlag(CypherFlags.HURT_OWNER)) return false
        // maybe hook
        return true
    }
    override fun onHitEntity(result: EntityHitResult) {
        super.onHitEntity(result)
        val entity = result.entity
        if (notHaveFlag(CypherFlags.NO_DAMAGE)) {
            val damage = getAttrOrProjDefault(CypherAttributes.DAMAGE)
            entity.hurt(damageSources().thrown(this, owner), damage.toFloat())
        }
        projHookContainer.playHooks(CypherBehaviorHookRegistry.HIT_ENTITY)
        { h, i -> h.onHitEntityServer(level(), this, i, entity) }
    }
    override fun onHitBlock(result: BlockHitResult) {
        super.onHitBlock(result)
    }


    // ==================================================================================================================
    // ==================================================================================================================
    override fun handleEntityEvent(id: Byte) {
        // trigger on client
        super.handleEntityEvent(id)
        if (id.toInt() == 3) {
            _cypher.visualEffectOnHit(level(), this)
        }
    }

    override fun handlePortal() {
        // TODO
        super.handlePortal()
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
    fun getAttribute(attr: CypherAttribute): Double? {
        return _attributeMap.get(attr)
    }
    fun getAttribute(holer: Holder<CypherAttribute>): Double? = getAttribute(holer.value())
    fun getAttrOrProjDefault(attr: CypherAttribute): Double = _attributeMap[attr] ?: _cypher.getAttrBaseOrDefault(attr)
    /** computedOperationMap > projectileCypher-base > attr#default */
    fun getAttrOrProjDefault(holer: Holder<CypherAttribute>): Double = getAttrOrProjDefault(holer.value())


    // ==================================================================================================================
    // ==================================================================================================================
    override fun readAdditionalSaveData(compound: CompoundTag) {
    }

    override fun addAdditionalSaveData(compound: CompoundTag) {
    }

    override fun getPickResult(): ItemStack? {
        // null by default, this is the creative mod middle button pick result
        return null
    }
    override fun isPickable(): Boolean {
        // false by default, entirely disable the picking activity
        return false
    }
    // a public method, to get a HitResult by checking if there is any block or entity in the direction of #getViewVector
    // ray cast
//    override fun pick(hitDistance: Double, partialTicks: Float, hitFluids: Boolean): HitResult {
//        return super.pick(hitDistance, partialTicks, hitFluids)
//    }


    protected fun discardCy(reason: DiscardReason) {
        when(reason){
            DiscardReason.ERASE -> {

            }
            else -> {
                if (level().isClientSide && reason == DiscardReason.EXPIRE) {
                    _cypher.visualEffectOnExpire(level(), this)
                }
                projHookContainer.playHooks(CypherBehaviorHookRegistry.BEFORE_DISCARD)
                { h, i -> h.beforeDiscardBoth(level(), this, i, reason) }
            }
        }
        discard()
    }



    // ==================================================================================================================
    // ==================================================================================================================
    private fun printModifiedAttrMap() {
        _attributeMap.forEach { a, v ->
            println("$a: $v")
        }
        if (_attributeMap.isEmpty()) println("projectile $_cypher has no modified attributes")
    }


    // check Entity.RemovalReason for more info
    // here only for cypher-projectile usage
    enum class DiscardReason {
        /** reach its time limit (e.g. naturally expire) */
        EXPIRE,
        /** through a collapse with entity */
        HIT_ENTITY,
        /** through a collapse with block */
        HIT_BLOCK,

        /** by some special reason */
        ERASE,
    }
}