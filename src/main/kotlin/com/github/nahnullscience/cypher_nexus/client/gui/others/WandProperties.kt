package com.github.nahnullscience.cypher_nexus.client.gui.others

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.utility.mod.ArrayOfCyphers
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import java.math.RoundingMode
import java.text.DecimalFormat

sealed class WandProperty <T : Any> (
    path: String,
) {
    val icon: Identifier = CypherNexus.modResource("textures/gui/$path.png")
    protected val text: String = "gui.cypher_nexus.wand.$path"
    open fun text(v: T): Component = Component.translatable(text, v)

    object ManaMax : WandProperty<Float>("mana_max") {
        override fun text(v: Float): Component {
            return Component.translatable(text, v.toInt())
        }
    }
    object ManaRegen : WandProperty<Float>("mana_regen") {
        override fun text(v: Float): Component {
            return Component.translatable(text, (v * 20).toInt())
        }
    }
    object CastDelay : WandProperty<Int>("cast_delay") {
        override fun text(v: Int): Component {
            return Component.translatable(text, v.tick2second())
        }
    }
    object RechargeTime : WandProperty<Int>("recharge_time") {
        override fun text(v: Int): Component {
            return Component.translatable(text, v.tick2second())
        }
    }
    object Spread : WandProperty<Float>("spread") {
        override fun text(v: Float): Component {
            return Component.translatable(text, df.format(v))
        }
    }
    object Draw : WandProperty<Int>("draw")
    object Capacity : WandProperty<Int>("capacity")
    object Etch : WandProperty<ArrayOfCyphers>("etch")

    companion object {
        val df = DecimalFormat("#.##").apply {
            roundingMode = RoundingMode.CEILING
        }

        fun Int.tick2second(): String {
            val s = toDouble() / 20
            return df.format(s)
        }
    }
}