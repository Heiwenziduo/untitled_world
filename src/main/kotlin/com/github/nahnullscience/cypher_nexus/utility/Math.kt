package com.github.nahnullscience.cypher_nexus.utility

import java.math.RoundingMode
import java.text.DecimalFormat

//fun <T> T.toSameSymbol(s: T): T where T : Number, T : Comparable<T> {
//    if (this == 0 || s == 0) return this
//    return if (this * s > 0) this else this * -1
//}

/**  */
fun Double.toSameSymbol(t: Double): Double {
    if (this == 0.0 || t == 0.0) return this
    return if (this * t > 0) this else this * -1
}

val dot0digit = DecimalFormat("#").apply {
    roundingMode = RoundingMode.CEILING
}
val dot1digit = DecimalFormat("#.#").apply {
    roundingMode = RoundingMode.CEILING
}
val dot2digit = DecimalFormat("#.##").apply {
    roundingMode = RoundingMode.CEILING
}

fun Int.tick2second(): String {
    val s = toDouble() / 20
    return dot2digit.format(s)
}