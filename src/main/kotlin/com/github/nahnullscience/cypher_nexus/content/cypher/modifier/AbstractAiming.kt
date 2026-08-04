package com.github.nahnullscience.cypher_nexus.content.cypher.modifier

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.ModDataAttachments.INVOKER_STATE_TRACKER
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap.Builder
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ModifierCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.TickBehaviorHook
import com.github.nahnullscience.cypher_nexus.utility.PlaneDefinition.Companion.sideOf
import com.github.nahnullscience.cypher_nexus.utility.PlaneDefinition.Companion.vectorToPlaneNormal
import com.github.nahnullscience.cypher_nexus.utility.plus
import com.github.nahnullscience.cypher_nexus.utility.rotateTowards
import com.github.nahnullscience.cypher_nexus.utility.times
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import kotlin.math.PI

abstract class AbstractAiming(
    defaultAttribute: Builder.() -> Builder
) : ModifierCypher(defaultAttribute), TickBehaviorHook {
    companion object {
        private const val ROTATION_RADIUS = PI / 12
        private const val PLANE_PUSH_FORCE = 0.04
        private const val CONCENTRATE_DIS_SQR = 4.0
    }

    class AimingArc(defaultAttribute: Builder.() -> Builder) : AbstractAiming(defaultAttribute) {
        override val resource = CypherNexus.modResource("aiming_arc")
        override fun <CE> onTick(
            index: Int,
            count: Int,
            level: Level,
            cyEntity: CE
        ) where CE : Entity, CE : ICypherEntity {
            if (cyEntity.tickCount < 5) return
            cyEntity.owner?.getData(INVOKER_STATE_TRACKER)?.viewPlane?.let { plane ->
                val delta = cyEntity.deltaMovement
                // if CE on the back side of the invoker, push it along the direction of invoker's view vector
                val sideForce = delta
                    .sideOf(plane)
                    .takeIf { it < 0 }
                    ?.let { plane.normal * (PLANE_PUSH_FORCE * count) }
                    ?: Vec3.ZERO

                cyEntity.deltaMovement = delta
                    .rotateTowards(plane.normal, ROTATION_RADIUS * count) + sideForce
            }
        }
    }

    class AimingReturn(defaultAttribute: Builder.() -> Builder) : AbstractAiming(defaultAttribute) {
        override val resource = CypherNexus.modResource("aiming_return")
        override fun <CE> onTick(
            index: Int,
            count: Int,
            level: Level,
            cyEntity: CE
        ) where CE : Entity, CE : ICypherEntity {
            if (cyEntity.tickCount < 5) return
            cyEntity.owner?.getData(INVOKER_STATE_TRACKER)?.viewPlane?.let { plane ->
                val delta = cyEntity.deltaMovement
                val sideForce = delta
                    .sideOf(plane)
                    .takeIf { it < 0 }
                    ?.let { plane.normal * (PLANE_PUSH_FORCE * count) }
                    ?: Vec3.ZERO

                // additionally concentrate CE to the center of invoker's view
                val pushCenter = cyEntity.position()
                    .vectorToPlaneNormal(plane)
                    .takeIf { it.lengthSqr() >= CONCENTRATE_DIS_SQR }
                    ?.let { it * (PLANE_PUSH_FORCE * count) }
                    ?: Vec3.ZERO

                cyEntity.deltaMovement = delta
                    .rotateTowards(plane.normal, ROTATION_RADIUS * count) + sideForce + pushCenter
            }
        }
    }
}