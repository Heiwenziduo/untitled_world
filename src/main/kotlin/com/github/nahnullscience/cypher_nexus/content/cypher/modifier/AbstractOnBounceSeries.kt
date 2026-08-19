package com.github.nahnullscience.cypher_nexus.content.cypher.modifier

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.mod.CypherSteerers.NO_STEERER
import com.github.nahnullscience.cypher_nexus.init.mod.Cyphers
import com.github.nahnullscience.cypher_nexus.init.mod.InvokingPatterns
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap.Builder
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ModifierCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.spawnCypherEntityRaw
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.OnBounceHook
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.AbstractInvokingPattern
import com.github.nahnullscience.cypher_nexus.utility.linear_space.AnchoredCoordinate
import com.github.nahnullscience.cypher_nexus.utility.linear_space.anchor
import com.github.nahnullscience.cypher_nexus.utility.linear_space.fromDirectionWithUpVector
import com.github.nahnullscience.cypher_nexus.utility.plus
import com.github.nahnullscience.cypher_nexus.utility.set
import com.github.nahnullscience.cypher_nexus.utility.times
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Vector3d

abstract class AbstractOnBounceSeries(
    defaultAttribute: Builder.() -> Builder
) : ModifierCypher(defaultAttribute), OnBounceHook {

    class ExplosiveBounce(defaultAttribute: Builder.() -> Builder) : AbstractOnBounceSeries(defaultAttribute) {
        override val resource = CypherNexus.modResource("explosive_bounce")
        override fun <CE> onBounce(
            index: Int,
            count: Int,
            level: Level,
            cyEntity: CE,
            bounceCount: Int,
            bounceSurface: Direction,
            bouncePoint: Vec3
        ) where CE : Entity, CE : ICypherEntity {
            // FIXME server explosion cause block states change thus may lead to different bounce trajectory
            cyEntity.explode(
                level,
                bouncePoint.x,
                bouncePoint.y,
                bouncePoint.z,
                0.5f
            )
        }
    }

    class JuxtaBounce(defaultAttribute: Builder.() -> Builder) : AbstractOnBounceSeries(defaultAttribute) {
        override val resource = CypherNexus.modResource("juxta_bounce")
        override fun <CE> onBounce(
            index: Int,
            count: Int,
            level: Level,
            cyEntity: CE,
            bounceCount: Int,
            bounceSurface: Direction,
            bouncePoint: Vec3
        ) where CE : Entity, CE : ICypherEntity {
            if (level !is ServerLevel) return
            val coo = AnchoredCoordinate.fromDirectionWithUpVector(
                bounceSurface,
                Vector3d().set(cyEntity.deltaMovement),
                cyEntity.random
            ).anchor(bouncePoint + bounceSurface.unitVec3 * 0.0625)

            val formation: AbstractInvokingPattern
            val count: Int
            if (cyEntity.ccMap?.containsKey(Cyphers.PHANTOM_RUSH) == true) {
                formation = InvokingPatterns.FRONT_DIFFUSE_OCTAGON_PATTERN.value()
                count = 8
            } else {
                formation = InvokingPatterns.FRONT_DIFFUSE_SQUARE_PATTERN.value()
                count = 4
            }

            for (i in 0 until count) {
                formation.layout(i, count, coo) { xp, yp, zp, xd, yd, zd ->
                    spawnCypherEntityRaw(
                        cyEntity.cypherHolder,
                        level,
                        NO_STEERER,
                        cyEntity.owner,
                        Vec3(xp, yp, zp),
                        Vec3(xd, yd, zd)
                    )
                }
            }
        }
    }
}