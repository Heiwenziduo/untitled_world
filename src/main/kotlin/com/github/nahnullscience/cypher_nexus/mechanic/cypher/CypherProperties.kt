package com.github.nahnullscience.cypher_nexus.mechanic.cypher

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.category.CypherCategory
import com.github.nahnullscience.cypher_nexus.utility.tick2second
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.Identifier

sealed class CypherProperties <T : Any> (
    path: String,
) {
    val icon: Identifier = CypherNexus.modResource("textures/gui/$path.png")
    protected val text: String = "gui.${CypherNexus.MOD_ID}.cypher.property.$path"
    open fun row(v: T): MutableComponent = Component.translatable(text, v)

    object CategoryRow : CypherProperties<CypherCategory>("category") {
        override fun row(v: CypherCategory): MutableComponent {
            val com = v.translation().withStyle(ChatFormatting.YELLOW)
            return Component.translatable(text, com)
        }
    }
    object ManaDrainRow : CypherProperties<Float>("mana_drain") {
        override fun row(v: Float): MutableComponent {
            val com = Component.literal(v.toInt().toString()).withStyle(ChatFormatting.BLUE)
            return Component.translatable(text, com)
        }
    }
    object CastDelayRow : CypherProperties<Int>("cast_delay") {
        override fun row(v: Int): MutableComponent {
            val withSymbol = if (v > 0) "+${v.tick2second()}" else v.tick2second()
            val com = Component.literal(withSymbol).withStyle(ChatFormatting.GRAY)
            return Component.translatable(text, com)
        }
    }
    object RechargeTimeRow : CypherProperties<Int>("recharge_time") {
        override fun row(v: Int): MutableComponent {
            val withSymbol = if (v > 0) "+${v.tick2second()}" else v.tick2second()
            val com = Component.literal(withSymbol).withStyle(ChatFormatting.GRAY)
            return Component.translatable(text, com)
        }
    }
    object DrawRow : CypherProperties<Int>("draw") {
        override fun row(v: Int): MutableComponent {
            val com = Component.literal(v.toString()).withStyle(ChatFormatting.GRAY)
            return Component.translatable(text, com)
        }
    }
}