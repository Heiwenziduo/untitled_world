package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.init.mod.CypherBehaviorHooks
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.AttributeOperator
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookContainer
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HooksSharedData
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileNode
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileStateChunk
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.StateChunkPool
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.TriggerType
import com.github.nahnullscience.cypher_nexus.utility.RayCastUtility
import com.github.nahnullscience.cypher_nexus.utility.VectorUtility
import com.github.nahnullscience.cypher_nexus.utility.i.IFlaggable
import com.github.nahnullscience.cypher_nexus.utility.mod.CNCodecs.MOCC_STREAM
import com.github.nahnullscience.cypher_nexus.utility.mod.MapOfCypherCounts
import com.github.nahnullscience.cypher_nexus.utility.mod.PosDirePair
import com.github.nahnullscience.cypher_nexus.utility.toSameDire
import com.github.nahnullscience.cypher_nexus.utility.toVec3i
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.Holder
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntitySpawnReason
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.animal.Animal
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Explosion
import net.minecraft.world.level.Level
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.minecraft.world.phys.*
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn
import java.util.*
import java.util.function.Consumer
import kotlin.jvm.optionals.getOrNull

abstract class AbstractCypherProjectile(
    entityType: EntityType<out AbstractCypherProjectile>,
    level: Level
) : Projectile(entityType, level), IEntityWithComplexSpawn,
    IFlaggable {
    companion object {
        const val CLIP_MARGIN = 0.2f
        const val CAPTURE_SIZE = 8.0
        const val CAPTURE_SIZE_SQR = CAPTURE_SIZE * CAPTURE_SIZE
        const val LOW_SPEED_THRESHOLD = 0.02
        const val LOW_SPEED_THRESHOLD_SQR = LOW_SPEED_THRESHOLD * LOW_SPEED_THRESHOLD

        /** generate projectile with attributes initialized */
        fun <T : AbstractCypherProjectile> create(
            entityType: EntityType<T>,
            level: ServerLevel,
            invoker: Entity?,
            direction: Vec3? = null,
            shotState: ProjectileStateChunk,
            node: ProjectileNode,
            stateHooks: HookContainer?
        ) : T {
            val proj = entityType.create(level, EntitySpawnReason.SPAWN_ITEM_USE) ?:
            throw IllegalStateException("Failed to create projectile [$entityType].")
            proj.initialize(invoker, direction, shotState, node, stateHooks)
            proj.mocc = shotState.cyphers
            return proj
        }

        fun <T : AbstractCypherProjectile> createRaw(entityType: EntityType<T>, level: ServerLevel) : T {
            val proj = entityType.create(level, EntitySpawnReason.SPAWN_ITEM_USE) ?:
            throw IllegalStateException("Failed to create projectile [$entityType].")
            return proj
        }
    }
    override fun defineSynchedData(builder: SynchedEntityData.Builder) { }
    override fun onSyncedDataUpdated(key: EntityDataAccessor<*>) {
        super.onSyncedDataUpdated(key)
    }
    override fun readAdditionalSaveData(input: ValueInput) = Unit
    override fun addAdditionalSaveData(output: ValueOutput) = Unit

    override fun writeSpawnData(buffer: RegistryFriendlyByteBuf) {
        // send when entity added to level
        buffer.writeBoolean(mocc != null) // write & read relay strictly on order, use a marker to tell client if a map follows
        if (mocc != null) {
            MOCC_STREAM.encode(buffer, mocc!!)
        }
    }

    override fun readSpawnData(buffer: RegistryFriendlyByteBuf) {
        if (buffer.readBoolean()) {
            val mocc = MOCC_STREAM.decode(buffer)
            initFromMoCC(mocc)
        } else mocc = null

        println("${level().isClientSide} client side hooks: $_hooks")
    }

    override fun onAddedToLevel() {
        super.onAddedToLevel()
    }

    override fun sendPairingData(serverPlayer: ServerPlayer, bundleBuilder: Consumer<CustomPacketPayload>) {
        super.sendPairingData(serverPlayer, bundleBuilder)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    abstract val cypherHolder: Holder<out AbstractProjectileCypher>
    val cypher get() = cypherHolder.value()

    /** a flag is basically a bundle of booleans */
    override var enabledFlags: Int = 0

    open var existing
        get() = getAttrOrProjDefault(CypherAttributes.EXISTING).toInt()
        set(value) {
            _attributeMap[CypherAttributes.EXISTING.value()] = CypherAttributes.EXISTING.value().restrictRange(value.toDouble())
        }
    /** initial speed, current "speed" is represented through #deltaMovement */
    val speed: Double get() = getAttrOrProjDefault(CypherAttributes.SPEED)
    val bounce: Int get() = getAttrOrProjDefault(CypherAttributes.BOUNCE).toInt()
    val gravity: Float get() = getAttrOrProjDefault(CypherAttributes.GRAVITY_FACTOR).toFloat()
    val speedFactor: Float get() = 1f - getAttrOrProjDefault(CypherAttributes.FRICTION_FACTOR).toFloat()

    var moveDirection: Vec3 = Vec3.ZERO

    // should be immutable after initialization
    private var _attributeMap = HashMap<CypherAttribute, Double>()
    protected var bounceCount = 0
    open val canBounce: Boolean get() = bounceCount < bounce
    /** store bounce points triggered in one tick */
    protected val bouncePoints = mutableListOf<Vec3>()
    protected val bounceTick get() = bouncePoints.isNotEmpty()
    private var lowSpeedTickCount = 0

    private var _hooks: HookContainer? = null
    val hooks: HookContainer? get() = _hooks
    val hooksSharedData = HooksSharedData()
    private var _trigger = TriggerType.NONE
    private var _payload: ProjectileStateChunk? = null
    val payload: ProjectileStateChunk? get() = _payload
    /** temporary server side cache */
    private var mocc: MapOfCypherCounts? = null

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // initialization
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private var isInitialized = false
    private fun initialize(
        invoker: Entity?,
        direction: Vec3? = null,
        shotState: ProjectileStateChunk,
        node: ProjectileNode,
        stateHooks: HookContainer?
    ) {
        if (isInitialized) CypherNexus.LOGGER.debug("{} is already initialized", this)
        setOwner(invoker)
        enabledFlags = shotState.enabledFlags or cypher.flags
        _payload = node.payload
        _trigger = node.trigger

        setHooks(stateHooks)
        initAttributes(shotState)
        setDirection(direction)

        debugMsg()
        isInitialized = true
    }

    private fun initFromMoCC(mocc: MapOfCypherCounts) {
        println("${level()} init from $mocc")
        val state = StateChunkPool.getOrCreateStateChunk(mocc)
        enabledFlags = state.enabledFlags or cypher.flags
        setHooks(state.hooks)
        initAttributes(state)
    }

    protected fun initAttributes(shootState: ProjectileStateChunk) {
        shootState.computedOperationMap.forEach { (attr, opMap) ->
            if (!attr.isProjectileAttribute) return@forEach
//            if (haveFlag(CypherFlags.CONSTANT_EXISTING) && CypherAttributes.EXISTING.`is`(attr.resource)) return@forEach
            // TODO prune cumulation, some of attributes will not be used, depends on cypher implementation

            _attributeMap.compute(attr) { a, v ->
                val def = cypher.getAttrBaseOrDefault(attr)
                val final = AttributeOperator.attributeCalculator(opMap, def)
                attr.restrictRange(final)
            }
        }
    }

    protected fun setDirection(direction: Vec3? = null) {
        moveDirection = direction?.normalize() ?: owner()?.lookAngle?.normalize() ?: moveDirection
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
    protected fun setHooks(container: HookContainer?) {
//        hooks = HookContainer(Optional.ofNullable(parent))
//        hooks?.add(cypher)
        _hooks = container
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    override fun tick() {
        // FIXME entity desync "positon-flash" almost always happen at around first time they sync
        // FIXME aiming deviation at high speed
        captureSurrounds()
        // start from tickCount == 1
        if (firstTick) onFirstTickBoth()
        if (tickCount == 20) trigger(TriggerType.TIMER_20)

//        updateInWaterStateAndDoFluidPushing()
//        updateFluidOnEyes()
//        updateSwimming()
        super.tick() // TODO: prune default tick

        projectileTick()

        if (existing < 0 || existing == tickCount) {
            // here's a trick, if player make existing-time exactly equal to 0, projectile will last till the game quit
            discardCypher(DiscardReason.EXPIRE)
        }

        if (deltaMovement.lengthSqr() <= LOW_SPEED_THRESHOLD_SQR) {
            onLowSpeedBoth(lowSpeedTickCount ++)
        } else lowSpeedTickCount = 0
    }

    /**
     * check hit-result and set delta-movement here
     * */
    protected open fun projectileTick() {
        tickBehaviorChangeBoth()

        updateRotation()
        applySpeedChange()
        applyGravity()

        tickMovementFinalizeBoth()

        /*
         * deltaMovement: the movement for the "next tick", client smooth animation relay on this
         * // an AABB check is used everyTick every vanilla projectile, sounds outrageous, but is ok in performance
         * */
        val hitResult = RayCastUtility.getProjectileHitResult(position(), this, ::canHitEntity, deltaMovement, level(), CLIP_MARGIN)
        bouncePoints.clear()
        val (lastBouncePoint, lastDeltaMove) = bounceLoop(hitResult)
        if (bounceTick) deltaMovement = deltaMovement.toSameDire(lastDeltaMove)

//        run collideCheck@ {
//            val fluidCheck = ClipContext.Fluid.NONE // bounce when touching water surface?
//            val blockHit = level().clip(ClipContext(position(), deltaMovement, ClipContext.Block.COLLIDER, fluidCheck, this))
//        }

        //checkInsideBlocks() // trigger #onInsideBlock

        // #move do exactly the same with #setPos when dealing with "noPhysics"
        if (bounceTick) setPos(lastBouncePoint.add(lastDeltaMove))
        else setPos(position().add(deltaMovement))
    }

    private fun captureSurrounds() {
        if (firstTick || tickCount and 3 == 3) { // trigger on 1, 3 and then every 4 tick
            val modules = _hooks?.get(CypherBehaviorHooks.ENTITY_SEARCH_BOTH)?.toList() ?: return
            var i = 0
            while (i < modules.size) {
                if (modules[i].first.needSearch(level(), this)) break
                i++
            }
            // if someone need a refresh-search
            if (i < modules.size || alwaysCaptureSurrounding()) {
                val entities = level().getEntities(this, boundingBox.inflate(CAPTURE_SIZE))
                { entity -> entity !is AbstractCypherProjectile }

//                println("capture $entities")
                for (entity in entities) {
                    onCaptureSurroundingBoth(entity)
                }
            }
        }
    }




    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // handle collapse
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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
            bounceCount++
            bouncePoints.add(hitPoint)

            // handle next bounce
            hitResultStep = RayCastUtility.getProjectileHitResult(startPosStep, this, ::canHitEntity, deltaMoveStep, level(), CLIP_MARGIN)

        } while (hitResultStep.type != HitResult.Type.MISS)

        return Pair(startPosStep, deltaMoveStep)
    }

    override fun canHitEntity(target: Entity): Boolean {
        if (!target.canBeHitByProjectile()) {
            return false // vanilla logic, for item-entities
        }
        // if (haveFlag(CypherFlags.NO_DAMAGE)) return false
        if (owner() == target && notHaveFlag(CypherFlags.HURT_OWNER)) return false
        // maybe hook
        return true
    }
    override fun onHit(result: HitResult) {
        super.onHit(result) // distribute hitResult
        onHitBoth(result)
        if (level().isClientSide) return
        trigger(TriggerType.COLLISION)

        val canPierce =
            result is BlockHitResult && haveFlag(CypherFlags.PIERCE_BLOCK) ||
            result is EntityHitResult && haveFlag(CypherFlags.PIERCE_ENTITY)
        if (!canPierce && !canBounce) {
            level().broadcastEntityEvent(this, 3) // combine with #handleEntityEvent
            discardCypher(if (result.type == HitResult.Type.BLOCK) DiscardReason.HIT_BLOCK else DiscardReason.HIT_ENTITY)
        }

    }
    override fun onHitEntity(result: EntityHitResult) {
        super.onHitEntity(result)
        val entity = result.entity
        if (notHaveFlag(CypherFlags.SKIP_DAMAGE_CHECK)) {
            val damage = getAttrOrProjDefault(CypherAttributes.DAMAGE)
            if (level() is ServerLevel)
            entity.hurtServer(level() as ServerLevel, damageSources().thrown(this, owner()), damage.toFloat())
        }
    }
    override fun onHitBlock(result: BlockHitResult) {
        super.onHitBlock(result)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // trigger & hooks
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /** ProjectileStateBlock#release */
    private fun releasePayload(posDire: PosDirePair) = _payload?.release(level(), this, owner(), posDire)
    fun trigger(type: TriggerType) {
        if (_trigger == TriggerType.NONE || type != _trigger) return
        val to = when(type) {
            // TODO apply a random offset
            TriggerType.COLLISION -> PosDirePair(position(), deltaMovement.reverse())
            else -> PosDirePair(position(), deltaMovement)
        }
        releasePayload(to)
    }

    open fun canHomeTarget(target: Entity): Boolean {
        return target !is Animal
                && target !is ItemEntity
                && !target.isInvisible
                && target.isAlive
                && target != owner()
    }
    /**
     * remember call super to function state hooks [CypherBehaviorHooks.BEFORE_DISCARD_BOTH]
     * */
    protected open fun onBeforeDiscardBoth(reason: DiscardReason) {
        _hooks?.playHooks(CypherBehaviorHooks.BEFORE_DISCARD_BOTH)
        { h, i -> h.beforeDiscardBoth(level(), this, i, reason) }
    }
    /**
     * remember call super to function state hooks [CypherBehaviorHooks.HIT_ENTITY_SERVER]
     * */
    protected open fun onHitBoth(result: HitResult) {
        if (!level().isClientSide) {
            _hooks?.playHooks(CypherBehaviorHooks.HIT_ENTITY_SERVER)
            { h, i -> h.onHitServer(level(), this, i, result) }
        }
    }
    /**
     * remember call super to function state hooks [CypherBehaviorHooks.FIRST_TICK_BOTH]
     * */
    protected open fun onFirstTickBoth() {
        _hooks?.playHooks(CypherBehaviorHooks.FIRST_TICK_BOTH)
        { h, i -> h.firstTickBoth(level(), this, i) }
    }
    /**
     * remember call super to function state hooks [CypherBehaviorHooks.TICK_BEHAVIOR_BOTH]
     * change speed / attributes (here) -> finalize movement -> bounce & hit check
     * */
    protected open fun tickBehaviorChangeBoth() {
        _hooks?.playHooks(CypherBehaviorHooks.TICK_BEHAVIOR_BOTH)
        { h, i -> h.tickBehaviorBoth(level(), this, i) }
    }
    /**
     * remember call super to function state hooks [CypherBehaviorHooks.TICK_MOVEMENT_FINALIZE_BOTH]
     * change speed / attributes -> finalize movement (here) -> bounce & hit check
     * */
    protected open fun tickMovementFinalizeBoth() {
        _hooks?.playHooks(CypherBehaviorHooks.TICK_MOVEMENT_FINALIZE_BOTH)
        { h, i -> h.finalizeTickMovementBoth(level(), this, i) }
    }
    /**
     * remember call super to function state hooks [CypherBehaviorHooks.ON_BOUNCE_BOTH]
     * */
    protected open fun onBounceBoth() {
        _hooks?.playHooks(CypherBehaviorHooks.ON_BOUNCE_BOTH)
        { h, i -> h.onBounceBoth(level(), this, i, bounceCount) }
    }
    /**  */
    protected open fun alwaysCaptureSurrounding() = false
    /**
     * remember call super to function state hooks [CypherBehaviorHooks.ENTITY_SEARCH_BOTH]
     * */
    protected open fun onCaptureSurroundingBoth(entity: Entity) {
        // TODO try further optimization, for this is O(m * n)
        _hooks?.playHooks(CypherBehaviorHooks.ENTITY_SEARCH_BOTH)
        { h, i -> h.entitySearchBoth(level(), this, i, entity) }
    }
    /**
     * remember call super to function state hooks []
     * */
    protected open fun onLowSpeedBoth(count: Int) {
        if (count < 60) return
        if (speed > LOW_SPEED_THRESHOLD) {
            discardCypher(DiscardReason.LOW_SPEED)
        }
    }

    /** client only */
    protected open fun discardVisualEffect() = Unit

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    override fun handleEntityEvent(id: Byte) {
        // trigger on client
        super.handleEntityEvent(id)
        if (id.toInt() == 3) {
            discardVisualEffect()
        }
    }

    override fun shouldRender(x: Double, y: Double, z: Double): Boolean = super.shouldRender(x, y, z)
    override fun shouldRenderAtSqrDistance(distance: Double): Boolean = distance < 4096

    override fun displayFireAnimation() = haveFlag(CypherFlags.WITH_FIRE)

    override fun isClientAuthoritative(): Boolean {
        // make boomerang client-track smooth
        return super.isClientAuthoritative() ||
                (haveFlag(CypherFlags.MOTION_FOLLOWS_OWNER) && owner()?.isClientAuthoritative == true) ||
                hooksSharedData.homingTarget?.isClientAuthoritative == true
    }
    override fun isLocalClientAuthoritative(): Boolean {
        // make boomerang client-track smooth
        return super.isLocalClientAuthoritative() ||
                (haveFlag(CypherFlags.MOTION_FOLLOWS_OWNER) && owner()?.isLocalClientAuthoritative == true) ||
                hooksSharedData.homingTarget?.isLocalClientAuthoritative == true
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    fun owner() = getOwner()
    fun getAttribute(attr: CypherAttribute): Double? = _attributeMap.get(attr)
    fun getAttribute(holer: Holder<CypherAttribute>): Double? = getAttribute(holer.value())
    fun getAttrOrProjDefault(attr: CypherAttribute): Double = _attributeMap[attr] ?: cypher.getAttrBaseOrDefault(attr)
    /** computedOperationMap > projectileCypher-base > attr#default */
    fun getAttrOrProjDefault(holer: Holder<CypherAttribute>): Double = getAttrOrProjDefault(holer.value())

    protected final override fun applyGravity() {
        if (gravity != 0f) deltaMovement = deltaMovement.add(0.0, -(gravity).toDouble(), 0.0)
    }
    protected fun applySpeedChange() {
        val f: Float = if (isInWater) underwaterSpeedFactor() * speedFactor else speedFactor
        if (f != 1f) deltaMovement = deltaMovement.scale(f.toDouble())
    }
    open fun underwaterSpeedFactor() = 0.8f
    fun addSpeed(impulse: Vec3) = addDeltaMovement(impulse)

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    protected fun discardCypher(reason: DiscardReason) {
        if (level().isClientSide) return
        trigger(TriggerType.DEATH)
        when(reason){
            DiscardReason.ERASE -> {}
            else -> {
                onBeforeDiscardBoth(reason)
            }
        }
        level().broadcastEntityEvent(this, 3)
        discard()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // miscellaneous
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    override fun getPickResult(): ItemStack? = null // null by default, this is the creative mod middle button pick result
    override fun isPickable() = false // false by default, entirely disable the picking activity
    /**
     * since the projectile can't exist without a related cypher,
     * the #deltaMovement initialization will be done automatically, call #shoot is not necessary
     * */
    override fun shoot(x: Double, y: Double, z: Double, velocity: Float, inaccuracy: Float) = Unit // do nothing, don't call
    override fun push(entity: Entity) = Unit
    override fun handlePortal() {
        // TODO
        super.handlePortal()
    }
    // a public method, to get a HitResult by checking if there is any block or entity in the direction of #getViewVector
    // ray cast
//    override fun pick(hitDistance: Double, partialTicks: Float, hitFluids: Boolean): HitResult {
//        return super.pick(hitDistance, partialTicks, hitFluids)
//    }
    override fun hurtServer(level: ServerLevel, source: DamageSource, damage: Float) = false
    override fun hurtClient(source: DamageSource) = false
    override fun ignoreExplosion(explosion: Explosion) = true // this prevents projectile getting kinetic energy from explosion
    override fun isAttackable(): Boolean = false
    override fun skipAttackInteraction(source: Entity): Boolean = true


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private fun debugMsg() {
        CypherNexus.LOGGER.debug("create projectile {}: {}", this, cypher)
        CypherFlags.printFlag(enabledFlags)

        // modified AttrMap
        _attributeMap.forEach { (a, v) ->
            println("$a: $v")
        }
        if (_attributeMap.isEmpty()) println("projectile $cypher has no modified attributes")
    }

    override fun hashCode() = super.hashCode()
    override fun equals(other: Any?) = if (other is Entity) other.id == this.id else false // a kotlin nullable reload
}