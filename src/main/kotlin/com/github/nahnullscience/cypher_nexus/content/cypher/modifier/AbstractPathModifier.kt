package com.github.nahnullscience.cypher_nexus.content.cypher.modifier

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ModifierCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HooksSharedData
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.TickMovementFinalizeHook
import com.github.nahnullscience.cypher_nexus.utility.coerceMaxLength
import com.github.nahnullscience.cypher_nexus.utility.headLeftVectorF
import com.github.nahnullscience.cypher_nexus.utility.CircleDefinition
import com.github.nahnullscience.cypher_nexus.utility.mostAlignedDirection
import com.github.nahnullscience.cypher_nexus.utility.plus
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.PI

abstract class AbstractPathModifier(
    path: String,
    defaultAttribute: CypherDataMap.Builder.() -> CypherDataMap.Builder
): ModifierCypher(defaultAttribute), TickMovementFinalizeHook {
    companion object {
        private const val ORBIT_RAD = (PI / 16).toFloat()

        private fun <CE> HooksSharedData<*>.initOrbitCircle(
            cyEntity: CE,
            target: Entity
        ): CircleDefinition where CE : Entity, CE : ICypherEntity {
            return orbitingCircle ?: run {
                val center = target.eyePosition
                val targetPos = cyEntity.position() + cyEntity.deltaMovement
                val radius = center.vectorTo(targetPos).coerceMaxLength(32.0)

                val radF = radius.toVector3f()
                val normal = Vector3f()

                radF.cross(0f, 1f, 0f, normal)

                if (normal.lengthSquared() > 1e-12f) {
                    normal.cross(radF).normalize()
                } else {
                    normal.set(target.headLeftVectorF())
                }

                CircleDefinition(center, radius, normal).also { orbitingCircle = it }
            }
        }
    }

    override val resource = CypherNexus.modResource(path)

    class HorizontalPath(
        defaultAttribute: CypherDataMap.Builder.() -> CypherDataMap.Builder
    ) : AbstractPathModifier("horizontal_path", defaultAttribute) {

        override fun <CE> finalizeTickMovement(
            index: Int,
            count: Int,
            level: Level,
            cyEntity: CE
        ) where CE : Entity, CE : ICypherEntity {
            cyEntity.deltaMovement = cyEntity.deltaMovement.horizontal()
        }
    }

    class CardinalPath(
        defaultAttribute: CypherDataMap.Builder.() -> CypherDataMap.Builder
    ) : AbstractPathModifier("cardinal_path", defaultAttribute) {

        override fun <CE> finalizeTickMovement(
            index: Int,
            count: Int,
            level: Level,
            cyEntity: CE
        ) where CE : Entity, CE : ICypherEntity {
            val t = cyEntity.deltaMovement.mostAlignedDirection()
            cyEntity.deltaMovement = cyEntity.deltaMovement.projectedOn(t.unitVec3)
        }
    }

    class PlaneOrbit(
        defaultAttribute: CypherDataMap.Builder.() -> CypherDataMap.Builder
    ) : AbstractPathModifier("plane_orbit", defaultAttribute) {

        override fun <CE> finalizeTickMovement(
            index: Int,
            count: Int,
            level: Level,
            cyEntity: CE
        ) where CE : Entity, CE : ICypherEntity {
            cyEntity.owner?.let { owner ->
                val circle = cyEntity.hooksSharedData.initOrbitCircle(cyEntity, owner)
                val phase = (cyEntity.tickCount - 1) and 31
                val radius = circle.radius.yRot(ORBIT_RAD * phase)
                val target = owner.eyePosition + radius
                cyEntity.deltaMovement = cyEntity.position().vectorTo(target)
            }
        }
    }

    class TrueOrbit(
        defaultAttribute: CypherDataMap.Builder.() -> CypherDataMap.Builder
    ) : AbstractPathModifier("true_orbit", defaultAttribute) {

        override fun <CE> finalizeTickMovement(
            index: Int,
            count: Int,
            level: Level,
            cyEntity: CE
        ) where CE : Entity, CE : ICypherEntity {
            cyEntity.owner?.let { owner ->
                val circle = cyEntity.hooksSharedData.initOrbitCircle(cyEntity, owner)
                val phase = (cyEntity.tickCount - 1) and 31
                val rotate = Quaternionf().fromAxisAngleRad(circle.normal, ORBIT_RAD * phase)
                val radius = circle.radius.toVector3f().rotate(rotate)
                val target = owner.eyePosition + radius
                cyEntity.deltaMovement = cyEntity.position().vectorTo(target)
            }
        }
    }
}