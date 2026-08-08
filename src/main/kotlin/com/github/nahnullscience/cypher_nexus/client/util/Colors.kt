package com.github.nahnullscience.cypher_nexus.client.util

import com.github.nahnullscience.cypher_nexus.utility.getArrayRGB
import net.minecraft.world.item.DyeColor
import java.awt.Color

object Colors {
    val vanillaDyeColorsFirework: List<FloatArray>

    init {
        val fireworkArray = Array(DyeColor.entries.size) { floatArrayOf() }
        DyeColor.entries.forEach { dyeColor ->
            val firework = Color(dyeColor.fireworkColor)
            fireworkArray[dyeColor.ordinal] = firework.getArrayRGB()
        }
        vanillaDyeColorsFirework = fireworkArray.asList()
    }
}