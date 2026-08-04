package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation

import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.init.mod.CypherHooks
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.DiscardReason
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity.Companion.CAPTURE_SIZE
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntityAttributeAccessor
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntityLogicContext
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntityPhysics
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.steerer.AbstractCypherSteerer
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileNode
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStatePool
import com.github.nahnullscience.cypher_nexus.utility.*
import com.github.nahnullscience.cypher_nexus.utility.exception.CypherEntityException
import com.github.nahnullscience.cypher_nexus.utility.mod.MapOfCypherCounts
import com.github.nahnullscience.cypher_nexus.utility.PosDirePair
import net.minecraft.core.Holder
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.*


class CypherEntityDelegation <CE> (
    val attribute: ICEAttribute = CEAttribute(),
    val context: ICEContext = CEContext<CE>(),
    val physics: ICEPhysics = CEPhysicsBasics<CE>()
) : ICypherEntity,
    ICypherEntityAttributeAccessor by attribute,
    ICypherEntityLogicContext by context,
    ICypherEntityPhysics by physics
    where CE : Entity, CE : ICypherEntity {


    private var _cyEntity: CE? = null
    protected val cyEntity: CE get() = _cyEntity ?:
    throw CypherEntityException("CypherEntityDelegation failed to initialize! make sure call #initEntity before it's adding to world!")
    override val cypherHolder: Holder<out AbstractProjectileCypher<*>> get() = cyEntity.cypherHolder // FIXME this may lead to infinite loop



    protected val level get() = cyEntity.level()
    protected val random get() = cyEntity.random

    override fun getDirectionInitial(): Vec3 = _initDirection ?: Vec3.ZERO
    override fun getPositionInitial(): Vec3  = _initPosition ?: cyEntity.owner?.position() ?: Vec3.ZERO

    protected var capturedInitialSpeedSqr: Double = 0.0


    protected var isInit: Boolean = false
    protected var lowSpeedTickCount = 0


    /**
     * if the entity has its own movement logic, set this to false
     * */
    open val moveAsProjectile: Boolean = true

    override fun initCypher(
        cypher: AbstractProjectileCypher<*>,
        ccMap: MapOfCypherCounts?,
        steerer: AbstractCypherSteerer
    ) {
        if (isInit) return
        if (ccMap == null) {
            attribute.initCypher(cypher, null)
            context.initCypher(cypher, null, steerer)
            physics.initCypher(cypher, null)
            isInit = true
            return
        } else {
            initCypher(cypher, ShotStatePool.getOrCreateShotState(ccMap), null, steerer)
        }
    }

    override fun initCypher(
        cypher: AbstractProjectileCypher<*>,
        shotState: ShotStateChunk,
        node: ProjectileNode?,
        steerer: AbstractCypherSteerer?
    ) {
        if (isInit) return
        attribute.initCypher(cypher, shotState)
        context.initCypher(cypher, shotState, steerer)
        physics.initCypher(cypher, shotState)
        isInit = true

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
        cyEntity.needsSync = true
    }

    protected open fun captureSurroundings() {
        if (cyEntity.tickCount == 1 || (cyEntity.tickCount - 2) and 3 == 3) { // trigger on tick 1, and then every 4 ticks
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





//    override fun onDealDamage(damage: Double) {}

}