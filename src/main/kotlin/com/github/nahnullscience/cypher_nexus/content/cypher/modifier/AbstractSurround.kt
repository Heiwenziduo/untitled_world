package com.github.nahnullscience.cypher_nexus.content.cypher.modifier

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.mod.CypherSteerers.ESCORT_SURROUND_STEERER
import com.github.nahnullscience.cypher_nexus.init.mod.Cyphers.SNOWBALL
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap.Builder
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ModifierCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.createCypherEntityRaw
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.TickBehaviorHook
import com.github.nahnullscience.cypher_nexus.utility.PosDirePair
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level

abstract class AbstractSurround(
    defaultAttribute: Builder.() -> Builder
) : ModifierCypher(defaultAttribute), TickBehaviorHook {
    companion object {
        private fun Int.isGenerateTime(): Boolean {
            return when(this) {
                5, 9, 13, 17 -> true
                else -> false
            }
        }
    }


    class SnowballSurrounding(
        defaultAttribute: Builder.() -> Builder
    ) : AbstractSurround(defaultAttribute) {
        override val resource = CypherNexus.modResource("snowball_surrounding")
        override fun <CE> onTick(
            index: Int,
            count: Int,
            level: Level,
            cyEntity: CE
        ) where CE : Entity, CE : ICypherEntity {
            val level = cyEntity.level() as? ServerLevel ?: return
            if (cyEntity.tickCount.isGenerateTime()) {
                val proj = createCypherEntityRaw(SNOWBALL, level, ESCORT_SURROUND_STEERER, cyEntity)
                proj.initDirection(PosDirePair(cyEntity.eyePosition))
                level.addFreshEntity(proj)
            }
        }
    }
}