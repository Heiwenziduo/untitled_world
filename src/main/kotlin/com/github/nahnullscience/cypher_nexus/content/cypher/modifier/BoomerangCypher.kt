package com.github.nahnullscience.cypher_nexus.content.cypher.modifier

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.content.cypher.modifier.AbstractTargetHoming.Companion.homeTo
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ModifierCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.TickBehaviorHook
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level

class BoomerangCypher(
    defaultAttribute: CypherDataMap.Builder.() -> CypherDataMap.Builder
) : ModifierCypher(defaultAttribute), TickBehaviorHook {
    companion object {
        private const val BOOMERANG_STRENGTH = 0.08
        private const val BOOMERANG_STRENGTH_LEVEL = 0.01
    }
    override val resource = CypherNexus.modResource("boomerang")

    override fun <CE> onTick(
        index: Int,
        count: Int,
        level: Level,
        cyEntity: CE
    ) where CE : Entity, CE : ICypherEntity {
        val target = cyEntity.owner ?: return
        cyEntity.homeTo(target, count * BOOMERANG_STRENGTH_LEVEL + BOOMERANG_STRENGTH)
    }
}