package com.github.nahnullscience.cypher_nexus.content.cypher.modifier

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ModifierCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.TickBehaviorHook
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import kotlin.math.min

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
        if (!target.boundingBox.contains(cyEntity.position())) {
            val dir = cyEntity.position().vectorTo(target.eyePosition)
            val dis =  min(dir.length(), count * BOOMERANG_STRENGTH_LEVEL + BOOMERANG_STRENGTH)
            val speed = dir.normalize().scale(dis)
            cyEntity.addDeltaMovement(speed)
        }
    }
}