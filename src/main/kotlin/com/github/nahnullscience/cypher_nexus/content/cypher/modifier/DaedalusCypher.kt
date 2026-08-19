package com.github.nahnullscience.cypher_nexus.content.cypher.modifier

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ModifierCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.invoking.ServerInvokeRedirectionHook
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotState.ShotStateViewer
import com.github.nahnullscience.cypher_nexus.utility.linear_space.AnchoredCoordinate
import com.github.nahnullscience.cypher_nexus.utility.linear_space.anchor
import com.github.nahnullscience.cypher_nexus.utility.linear_space.face
import com.github.nahnullscience.cypher_nexus.utility.nearestHitPointThen
import com.github.nahnullscience.cypher_nexus.utility.randomInCone
import com.github.nahnullscience.cypher_nexus.utility.set
import com.github.nahnullscience.cypher_nexus.utility.toVec3
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.ClipContext.Block
import net.minecraft.world.level.ClipContext.Fluid
import net.minecraft.world.phys.HitResult.Type
import org.joml.Vector3d

class DaedalusCypher(
    defaultAttribute: CypherDataMap.Builder.() -> CypherDataMap.Builder
) : ModifierCypher(defaultAttribute), ServerInvokeRedirectionHook {
    companion object {
        private const val MARGIN = 0.3
    }

    override val resource = CypherNexus.modResource("daedalus")

    override fun invokeRedirectServer(
        index: Int,
        count: Int,
        level: ServerLevel,
        owner: Entity?,
        state: ShotStateViewer,
        coordinate: AnchoredCoordinate,
        directInvoker: Entity?
    ) {
        if (directInvoker == null) return

        val angle = 24.0
        val heightMax = (16.0 + 8.0 * count).coerceAtMost(128.0)
        val lengthMax = (16.0 + 8.0 * count).coerceAtMost(64.0)

        var vf = coordinate.tmpV3f
        val vd0 = coordinate.tmpV3d
        val vd1 = Vector3d()

        val start = vd0.set(coordinate.anchor).toVec3()
        val route = vd1.set(coordinate.front).mul(lengthMax)
        var hitDestination = vd0.add(route).toVec3()

        var hitDir: Direction? = null
        level.nearestHitPointThen(start, hitDestination, directInvoker, MARGIN) { hitPoint, dir ->
            hitDestination = hitPoint
            hitDir = dir
        }


        if (hitDir != Direction.DOWN) {
            vf = vf.set(0f, 1f, 0f).randomInCone(angle, directInvoker.random)
            vf.mul(heightMax.toFloat())
            val upward = vf.toVec3()
            val blockResult2 = level.clipIncludingBorder(
                ClipContext(hitDestination, hitDestination.add(upward), Block.COLLIDER, Fluid.NONE, directInvoker)
            )

            if (blockResult2.type != Type.MISS) {
                vd0.set(blockResult2.location).sub(0.0, 0.25, 0.0)
            } else {
                vd0.set(hitDestination.x + upward.x, hitDestination.y + upward.y, hitDestination.z + upward.z)
            }
            vd1.set(hitDestination).sub(vd0)
        } else {
            // if targeting a ceiling, fire projectiles like a shower
            val downward = vf.set(0f, -1f, 0f).randomInCone(angle, directInvoker.random)
            vd0.set(hitDestination).sub(0.0, 0.25, 0.0)
            vd1.set(downward)
        }

        coordinate.anchor(vd0)
        coordinate.face(vd1)
    }
}