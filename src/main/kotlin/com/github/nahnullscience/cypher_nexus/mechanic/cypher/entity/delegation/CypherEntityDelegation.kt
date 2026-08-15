package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation

import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntityAttributeAccessor.Companion.getAttributeOrDefault
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntityLogicContext
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntityPhysics
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.steerer.AbstractCypherSteerer
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileNode
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStatePool
import com.github.nahnullscience.cypher_nexus.utility.*
import com.github.nahnullscience.cypher_nexus.utility.exception.CypherEntityException
import com.github.nahnullscience.cypher_nexus.utility.mod.MapOfCypherCounts
import com.github.nahnullscience.cypher_nexus.utility.linear_space.PosDirePair
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.*


class CypherEntityDelegation <CE> (
    val context: ICEContext<CE> = CEContext(),
    val physics: ICEPhysics<CE> = CEPhysicsBasics()
) : ICypherEntity,
    ICypherEntityLogicContext by context,
    ICypherEntityPhysics by physics
    where CE : Entity, CE : ICypherEntity
{
    var isInit: Boolean = false
        private set


    override val cypherHolder get() =
        throw CypherEntityException("field #cypherHolder should be implemented by the concrete cypher-entity.")

//    private var _cyEntity: CE? = null
//    private val cyEntity: CE get() = _cyEntity ?:
//        throw CypherEntityException("CypherEntityDelegation failed to initialize! make sure call #initEntity before it's adding to world!")


    override fun initCypher(
        cypher: AbstractProjectileCypher<*>,
        ccMap: MapOfCypherCounts?,
        steerer: AbstractCypherSteerer
    ) {
        if (isInit) return
        if (ccMap == null) {
            context.initCypher(cypher, null, steerer)
            physics.initCypher(cypher, null, null)
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
        context.initCypher(cypher, shotState, steerer)
        physics.initCypher(cypher, shotState, node)
        isInit = true

    }

    override fun <T> initEntity (ce: T) where T : Entity, T : ICypherEntity {
        @Suppress("UNCHECKED_CAST")
        ce as CE
        context.initEntity(ce)
        physics.initEntity(ce)
        initDirection(ce)
    }


    override fun getDirectionInitial(): Vec3 = _initDirection ?: Vec3.ZERO
    override fun getPositionInitial(): Vec3  = _initPosition ?: owner?.position() ?: Vec3.ZERO

    private var _initPosition: Vec3? = null // due to CyEntity init timing, remember direction data and init later
    private var _initDirection: Vec3? = null
    override fun initDirection(pair: PosDirePair) = run { _initDirection = pair.direction; _initPosition = pair.position }
    private fun initDirection(ce: CE) {
        if (ce.level.isClientSide) return
        // when initDirection didn't call, vanilla setPos can handle it, with a direction ZERO
        if (_initPosition == null || _initDirection == null) return

        _initDirection = _initDirection ?: ce.owner?.headLookAngle

        ce.getPositionInitial().let { ce.setPos(it) }
        ce.getDirectionInitial().let {
            if (it == Vec3.ZERO) ce.deltaMovement = Vec3.ZERO
            else {
                ce.deltaMovement = it.normalize().scale(ce.getAttributeOrDefault(CypherAttributes.SPEED_INITIAL))
                ce.rotateTowardSpeed(1f)
            }
        }
        ce.needsSync = true
    }

    override fun printDebugMsg(o: Any?) {
        context.printDebugMsg(o)
        physics.printDebugMsg(o)
    }
}