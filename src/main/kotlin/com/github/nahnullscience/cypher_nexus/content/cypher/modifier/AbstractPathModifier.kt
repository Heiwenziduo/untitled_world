package com.github.nahnullscience.cypher_nexus.content.cypher.modifier

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap.Builder
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ModifierCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.AttributeOperator
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractCypherProjectile
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.BothTickMovementFinalizeHook
import net.minecraft.core.Direction
import net.minecraft.world.level.Level

abstract class AbstractPathModifier(
    path: String,
    private val _manaDrain: Float,
    private val damage: Double = 0.0,
): ModifierCypher(), BothTickMovementFinalizeHook {

    override val resource = CypherNexus.modResource(path)
    override fun defaultAttributes(): Builder {
        return super.defaultAttributes()
            .manaDrain(_manaDrain)
            .stateChunkAttr(CypherAttributes.DAMAGE, AttributeOperator.ADD, damage)
    }

    object HorizontalPath : AbstractPathModifier("horizontal_path", 0f, 0.5) {
        override fun finalizeTickMovementBoth(
            level: Level,
            projectile: AbstractCypherProjectile,
            strength: Int
        ) {
            projectile.deltaMovement = projectile.deltaMovement.horizontal()
        }
    }

    object CardinalPath : AbstractPathModifier("cardinal_path", 0f, 0.5) {
        override fun finalizeTickMovementBoth(
            level: Level,
            projectile: AbstractCypherProjectile,
            strength: Int
        ) {
            val t = Direction.getApproximateNearest(projectile.deltaMovement)
            projectile.deltaMovement = projectile.deltaMovement.projectedOn(t.unitVec3)
            projectile.hooksSharedData.pathDirection = t
        }
    }
}