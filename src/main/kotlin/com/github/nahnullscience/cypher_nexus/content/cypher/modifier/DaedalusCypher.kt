package com.github.nahnullscience.cypher_nexus.content.cypher.modifier

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ModifierCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.invoking.ServerInvokePosRedirectionHook
import com.github.nahnullscience.cypher_nexus.utility.LevelUtil.forEachEntityWithin
import com.github.nahnullscience.cypher_nexus.utility.mod.PosDirePair
import com.github.nahnullscience.cypher_nexus.utility.randomInCone
import com.github.nahnullscience.cypher_nexus.utility.rayCastThen
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.ClipContext.Block
import net.minecraft.world.level.ClipContext.Fluid
import net.minecraft.world.phys.HitResult.Type
import net.minecraft.world.phys.Vec3

class DaedalusCypher(
    defaultAttribute: CypherDataMap.Builder.() -> CypherDataMap.Builder
) : ModifierCypher(defaultAttribute), ServerInvokePosRedirectionHook {
    companion object {
        private const val MARGIN = 0.3
    }

    override val resource = CypherNexus.modResource("daedalus")

    override fun redirectPosDireServer(
        level: ServerLevel,
        directInvoker: Entity?,
        owner: Entity?,
        strength: Int,
        pair: PosDirePair,
        index: Int
    ): PosDirePair {
        if (directInvoker == null) return pair

        val heightMax = (16.0 + 8.0 * strength).coerceAtMost(128.0)
        val lengthMax = (16.0 + 8.0 * strength).coerceAtMost(128.0)
        val (start, direction) = pair
        if (direction != Vec3.ZERO) {
            val routeDefault = direction.normalize().scale(lengthMax)
            var destination = start.add(routeDefault)
            val blockResult = level.clipIncludingBorder(
                ClipContext(start, destination, Block.COLLIDER, Fluid.NONE, directInvoker)
            )
            if (blockResult.type != Type.MISS) {
                destination = blockResult.location
            }
            var nearest = Double.MAX_VALUE
            var targetEntity: Entity? = null
            level.forEachEntityWithin(
                directInvoker,
                directInvoker.boundingBox.expandTowards(destination.subtract(start)),
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
            val upward = Vec3.Y_AXIS.randomInCone(20.0, directInvoker.random).scale(heightMax) // TODO use SPREAD as factor
            val blockResult2 = level.clipIncludingBorder(
                ClipContext(destination, destination.add(upward), Block.COLLIDER, Fluid.NONE, directInvoker)
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