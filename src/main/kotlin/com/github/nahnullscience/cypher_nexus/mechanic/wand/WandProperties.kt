package com.github.nahnullscience.cypher_nexus.mechanic.wand

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.utility.dot2digit
import com.github.nahnullscience.cypher_nexus.utility.mod.ArrayOfCyphers
import com.github.nahnullscience.cypher_nexus.utility.tick2second
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.Identifier

sealed class WandProperties <T : Any> (
    path: String,
) {
    val icon: Identifier = CypherNexus.modResource("textures/gui/$path.png")
    protected val text: String = "gui.${CypherNexus.MOD_ID}.wand.$path"
    open fun row(v: T): MutableComponent = Component.translatable(text, v)

    object ManaMaxRow : WandProperties<Float>("mana_max") {
        override fun row(v: Float): MutableComponent {
            return Component.translatable(text, v.toInt())
        }
    }
    object ManaRegenRow : WandProperties<Float>("mana_regen") {
        override fun row(v: Float): MutableComponent {
            return Component.translatable(text, (v * 20).toInt())
        }
    }
    object WandCastDelayRow : WandProperties<Int>("cast_delay") {
        override fun row(v: Int): MutableComponent {
            return Component.translatable(text, v.tick2second())
        }
    }
    object WandRechargeTimeRow : WandProperties<Int>("recharge_time") {
        override fun row(v: Int): MutableComponent {
            return Component.translatable(text, v.tick2second())
        }
    }
    object SpreadRow : WandProperties<Float>("spread") {
        override fun row(v: Float): MutableComponent {
            return Component.translatable(text, dot2digit.format(v))
        }
    }
    object WandDrawRow : WandProperties<Int>("draw")
    object CapacityRow : WandProperties<Int>("capacity")
    object EtchRow : WandProperties<ArrayOfCyphers>("etch")

}