package com.github.nahnullscience.cypher_nexus.content.cypher.modifier

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap.Builder
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ModifierCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.AttributeOperator
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.TickMovementFinalizeHook
import net.minecraft.core.Direction
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level

abstract class AbstractPathModifier(
    path: String,
    defaultAttribute: CypherDataMap.Builder.() -> CypherDataMap.Builder
): ModifierCypher(defaultAttribute), TickMovementFinalizeHook {

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
            val t = Direction.getApproximateNearest(cyEntity.deltaMovement)
            cyEntity.deltaMovement = cyEntity.deltaMovement.projectedOn(t.unitVec3)
            cyEntity.hooksSharedData.pathDirection = t
        }
    }
}