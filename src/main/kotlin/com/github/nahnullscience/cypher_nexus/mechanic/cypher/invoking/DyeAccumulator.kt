package com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking

import com.github.nahnullscience.cypher_nexus.utility.getArrayRGBA
import java.awt.Color
import kotlin.math.pow

class DyeAccumulator {
    private var resolvedColor: Int = -1
    private var resolvedColorArray: FloatArray = floatArrayOf()

    private var lock = false

    var isResolved = false
        private set

    val color: Int get() = if (isResolved) resolvedColor else 0xFFFF_FFFF.toInt()

    val colorArray: FloatArray
        get() = resolvedColorArray

    private var totalR = 0f
    private var totalG = 0f
    private var totalB = 0f

    private var alphaMultiplier = 1f  // Range: 0.0f (Transparent) to 1.0f (Opaque)
    private var brightnessShift = 0f   // Range: -1.0f (Pure Black) to +1.0f (Pure White)

    var dyeCount = 0
        private set



    fun addDye(r: Float, g: Float, b: Float, count: Int = 1) = apply {
        if (!lock) {
            dyeCount += count
            totalR += r * count
            totalG += g * count
            totalB += b * count
        }
    }

    fun addDye(color: Color, count: Int = 1) = apply {
        if (!lock) {
            dyeCount += count
            totalR += color.red.toFloat() * count / 255
            totalG += color.green.toFloat() * count / 255
            totalB += color.blue.toFloat() * count / 255
        }
    }

    fun multiplyAlpha(factor: Float, count: Int = 1) = apply {
        if (!lock) alphaMultiplier *= factor.pow(count)
    }
    fun adjustBrightness(factor: Float, count: Int = 1) = apply {
        if (!lock) brightnessShift += factor * count
    }

    /**
     * Resolves accumulated dyes into a single packed ARGB integer.
     */
    fun resolveColor(defaultColor: Int = 0xFFFFFFFF.toInt()): Int? {
        if (lock) return resolvedColor
        // If no dye modifiers were applied, keep the default base color
        if (dyeCount == 0 && brightnessShift == 0f && alphaMultiplier == 1f) {
            return resolve(defaultColor)
        }
        else if (alphaMultiplier < 1e-6) { return resolve(0) }

        // 1. Calculate algebraic mean RGB (0.0 to 1.0)
        val avgR = if (dyeCount > 0) totalR / dyeCount else 1.0f
        val avgG = if (dyeCount > 0) totalG / dyeCount else 1.0f
        val avgB = if (dyeCount > 0) totalB / dyeCount else 1.0f

        // 2. Convert RGB to HSL for clean Lighten/Darken adjustments
        val hsl = FloatArray(3)
        Color.RGBtoHSB((avgR * 255).toInt(), (avgG * 255).toInt(), (avgB * 255).toInt(), hsl)

        // 3. Apply lightness shift (-1.0 to +1.0) to Lightness channel (hsl[2])
        hsl[2] = (hsl[2] + brightnessShift).coerceIn(0f, 1f)

        // 4. Convert HSL back to RGB
        val adjustedRgb = Color.HSBtoRGB(hsl[0], hsl[1], hsl[2])
        val r = (adjustedRgb shr 16) and 0xFF
        val g = (adjustedRgb shr 8) and 0xFF
        val b = adjustedRgb and 0xFF

        // 5. Calculate alpha channel (0 to 255)
        val defaultAlpha = (defaultColor shr 24) and 0xFF
        val finalAlpha = (defaultAlpha * alphaMultiplier).toInt().coerceIn(0, 255)

        // 6. Pack back into 32-bit ARGB
        return resolve((finalAlpha shl 24) or (r shl 16) or (g shl 8) or b)
    }

    private fun resolve(v: Int) = run {
        lock = true
        if (v == 0xFFFFFFFF.toInt()) return@run null
        else v
    }?.also {
        isResolved = true
        resolvedColor = it
        resolvedColorArray = it.getArrayRGBA()
    }
}