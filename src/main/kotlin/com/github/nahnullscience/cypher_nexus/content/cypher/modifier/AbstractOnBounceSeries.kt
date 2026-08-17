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
import com.github.nahnullscience.cypher_nexus.utility.linear_space.CoordinateDefinition
import com.github.nahnullscience.cypher_nexus.utility.linear_space.PosDirePair
import com.github.nahnullscience.cypher_nexus.utility.plus
import com.github.nahnullscience.cypher_nexus.utility.randomPerpendicularNormal
import com.github.nahnullscience.cypher_nexus.utility.times
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3

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
            val co = CoordinateDefinition.faceDirectionWithUpVector(bounceSurface, cyEntity.deltaMovement) {
                bounceSurface.axis.randomPerpendicularNormal(cyEntity.random)
            }
            val base = PosDirePair(bouncePoint + bounceSurface.unitVec3 * 0.01, bounceSurface.unitVec3)

            val formation: AbstractInvokingPattern
            val count: Int
            if (cyEntity.ccMap?.containsKey(Cyphers.PHANTOM_RUSH) == true) {
                formation = InvokingPatterns.FRONT_OCTAGON_PATTERN.value()
                count = 8
            } else {
                formation = InvokingPatterns.FRONT_SQUARE_PATTERN.value()
                count = 4
            }

            for (i in 0 until count) {
                val pair = formation.layout(i, count, co)
                spawnCypherEntityRaw(
                    cyEntity.cypherHolder,
                    level,
                    NO_STEERER,
                    pair
                )
            }
        }
    }
}