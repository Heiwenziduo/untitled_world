package com.github.nahnullscience.cypher_nexus.utility

import java.awt.Color

/** r g b a in order */
fun Color.getArrayRGBA(): FloatArray {
    val r = red.toFloat() / 255
    val g = green.toFloat() / 255
    val b = blue.toFloat() / 255
    val a = alpha.toFloat() / 255
    return floatArrayOf(r, g, b, a)
}

fun Color.getArrayRGB(): FloatArray {
    val r = red.toFloat() / 255
    val g = green.toFloat() / 255
    val b = blue.toFloat() / 255
    return floatArrayOf(r, g, b)
}

fun Int.toRGB(): Color = Color(this, false)

fun Int.toARGB(): Color = Color(this, true)

/**
 * r -> g -> b -> a
 * */
fun Int.getArrayRGBA(): FloatArray {
    val a = ((this shr 24) and 0xFF) / 255f
    val r = ((this shr 16) and 0xFF) / 255f
    val g = ((this shr 8) and 0xFF) / 255f
    val b = (this and 0xFF) / 255f
    return floatArrayOf(r, g, b, a)
}