package com.github.nahnullscience.cypher_nexus.content.cypher.modifier

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.DedicatedCypherProjectile
import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ModifierCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.AttributeOperator
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation.ICypherBeforeInit
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation.ICypherEntity.Companion.HIT_BB_INFLATION
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.invoking.ServerInvokeRedirectPosHook
import com.github.nahnullscience.cypher_nexus.utility.LevelUtil.forEachEntityWithin
import com.github.nahnullscience.cypher_nexus.utility.RayCastUtility
import com.github.nahnullscience.cypher_nexus.utility.RayCastUtility.rayCastThen
import com.github.nahnullscience.cypher_nexus.utility.mod.PosDirePair
import com.github.nahnullscience.cypher_nexus.utility.randomInCone
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.ClipContext.Block
import net.minecraft.world.level.ClipContext.Fluid
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.HitResult.Type
import net.minecraft.world.phys.Vec3

object DaedalusCypher : ModifierCypher(), ServerInvokeRedirectPosHook {
    const val MARGIN = 0.3

    override val resource = CypherNexus.modResource("daedalus")

    override fun defaultAttributes(): CypherDataMap.Builder {
        return super.defaultAttributes()
            .manaDrain(50f)
            .stateChunkAttr(CypherAttributes.SPEED, AttributeOperator.MULTIPLY_TOTAL, 1.25)
            .stateChunkAttr(CypherAttributes.RECOIL, AttributeOperator.MULTIPLY_TOTAL, 0.0)
            .stateChunkAttr(CypherAttributes.SPREAD, AttributeOperator.ADD, 20.0)
            .stateChunkAttr(CypherAttributes.GRAVITY_FACTOR, AttributeOperator.ADD, 0.03)
    }

    override fun <CypherBeforeInit> redirectPosDireServer(
        level: ServerLevel,
        invoker: Entity?,
        owner: Entity?,
        cypherEntity: CypherBeforeInit,
        strength: Int,
        pair: PosDirePair,
        index: Int
    ): PosDirePair where CypherBeforeInit : Entity, CypherBeforeInit : ICypherBeforeInit {
        if (invoker == null) return pair

        val heightMax = (16.0 + 8.0 * strength).coerceAtMost(128.0)
        val lengthMax = (16.0 + 8.0 * strength).coerceAtMost(128.0)
        val (start, direction) = pair
        if (direction != Vec3.ZERO) {
            val routeDefault = direction.normalize().scale(lengthMax)
            var destination = start.add(routeDefault)
            val blockResult = level.clipIncludingBorder(
                ClipContext(start, destination, Block.COLLIDER, Fluid.NONE, invoker)
            )
            if (blockResult.type != Type.MISS) {
                destination = blockResult.location
            }
            var nearest = Double.MAX_VALUE
            var targetEntity: Entity? = null
            level.forEachEntityWithin(
                invoker,
                invoker.boundingBox.expandTowards(destination.subtract(start)),
                { true }
            ) { target ->
                start.rayCastThen(destination, target.boundingBox, MARGIN) { hitPoint, dir ->
                    val dd: Double = start.distanceToSqr(hitPoint)
                    if (dd < nearest) {
                        targetEntity = target
                        nearest = dd
                        destination = hitPoint
                    }
                }
            }
            if (targetEntity != null) {
                destination = targetEntity.position()
            }
            val upward = Vec3(0.0, 1.0, 0.0).randomInCone(20.0, invoker.random).scale(heightMax) // TODO use SPREAD as factor
            val blockResult2 = level.clipIncludingBorder(
                ClipContext(destination, destination.add(upward), Block.COLLIDER, Fluid.NONE, invoker)
            )
            val posFinal =
                if (blockResult2.type != Type.MISS) blockResult2.location.subtract(0.0, 0.1, 0.0)
                else destination.add(upward)
            return PosDirePair(posFinal, posFinal.vectorTo(destination))
//                .also { println("DaedalusCypher before $pair, after $it") }
        }
        return pair
    }

}