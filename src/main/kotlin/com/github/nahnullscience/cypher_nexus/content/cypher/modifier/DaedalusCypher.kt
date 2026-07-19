package com.github.nahnullscience.cypher_nexus.content.cypher.modifier

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ModifierCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.invoking.ServerInvokePosRedirectionHook
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk.ShotStateViewer
import com.github.nahnullscience.cypher_nexus.utility.mod.PosDirePair
import com.github.nahnullscience.cypher_nexus.utility.nearestHitPointThen
import com.github.nahnullscience.cypher_nexus.utility.randomInCone
import net.minecraft.core.Direction
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
        index: Int,
        count: Int,
        level: ServerLevel,
        owner: Entity?,
        state: ShotStateViewer,
        directInvoker: Entity?,
        pair: PosDirePair
    ): PosDirePair {
        if (directInvoker == null) return pair

        val heightMax = (16.0 + 8.0 * count).coerceAtMost(128.0)
        val lengthMax = (16.0 + 8.0 * count).coerceAtMost(128.0)
        val (start, direction) = pair
        if (direction != Vec3.ZERO) {
            val angle = 20.0 // TODO use SPREAD as factor
            val routeDefault = direction.normalize().scale(lengthMax)
            var hitDestination = start.add(routeDefault)
            var hitDir = Direction.UP
            level.nearestHitPointThen(start, hitDestination, directInvoker, MARGIN) { hitPoint, dir ->
                hitDestination = hitPoint
                hitDir = dir
            }
            if (hitDir != Direction.DOWN) {
                val upward = Vec3.Y_AXIS.randomInCone(angle, directInvoker.random).scale(heightMax)
                val blockResult2 = level.clipIncludingBorder(
                    ClipContext(hitDestination, hitDestination.add(upward), Block.COLLIDER, Fluid.NONE, directInvoker)
                )
                val posFinal =
                    if (blockResult2.type != Type.MISS) blockResult2.location.subtract(0.0, 0.1, 0.0)
                    else hitDestination.add(upward)
                return PosDirePair(posFinal, posFinal.vectorTo(hitDestination))
//                .also { println("DaedalusCypher before $pair, after $it") }
            } else {
                // if targeting a ceiling, fire projectiles like a shower
                hitDestination = hitDestination.subtract(0.0, 0.1, 0.0)
                val downward = Vec3(0.0, -1.0, 0.0).randomInCone(angle, directInvoker.random)
                return PosDirePair(hitDestination, downward)
            }
        }
        return pair
    }

}