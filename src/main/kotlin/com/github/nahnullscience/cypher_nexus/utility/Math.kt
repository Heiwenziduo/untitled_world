package com.github.nahnullscience.cypher_nexus.utility

import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.PI
import kotlin.math.sqrt

//fun <T> T.toSameSymbol(s: T): T where T : Number, T : Comparable<T> {
//    if (this == 0 || s == 0) return this
//    return if (this * s > 0) this else this * -1
//}

const val RAD_2_ANG = 180.0 / PI
const val RAD_2_ANG_F = RAD_2_ANG.toFloat()
const val ANG_2_RAD = PI / 180.0
const val ANG_2_RAD_F = ANG_2_RAD.toFloat()


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

/**
 * execute [task] on points within `from` to `to` in an interval of given gap.
 * e.g. once a gap distance has been traveled
 * @see [linearInterpolateTimes]
 * */
inline fun linearInterpolateGaps(
    fromX: Double, fromY: Double, fromZ: Double,
    toX: Double, toY: Double, toZ: Double,
    gap: Double,
    atLeastOnce: Boolean = true,
    task: (step: Int, x: Double, y: Double, z: Double) -> Unit,
) {
    if (gap <= 0.0) {
        if (atLeastOnce) task(0, fromX, fromY, fromZ)
        return
    }

    val dx = toX - fromX
    val dy = toY - fromY
    val dz = toZ - fromZ
    val distSq = dx * dx + dy * dy + dz * dz

    if (distSq < gap * gap) {
        if (atLeastOnce) task(0, fromX, fromY, fromZ)
        return
    }

    val dist = sqrt(distSq)
    val stepFactor = gap / dist
    val stepX = dx * stepFactor
    val stepY = dy * stepFactor
    val stepZ = dz * stepFactor

    val steps = (dist / gap).toInt()
    var currX = fromX
    var currY = fromY
    var currZ = fromZ

    for (i in 0..steps) {
        task(i, currX, currY, currZ)
        currX += stepX
        currY += stepY
        currZ += stepZ
    }
}

/**
 * execute [task] given times on points evenly distributed through 'from' to 'to'
 * @see [linearInterpolateGaps]
 * */
inline fun linearInterpolateTimes(
    fromX: Double, fromY: Double, fromZ: Double,
    toX: Double, toY: Double, toZ: Double,
    times: Int,
    task: (step: Int, x: Double, y: Double, z: Double) -> Unit
) {
    if (times <= 0) return
    if (times == 1) {
        task(0, fromX, fromY, fromZ)
        return
    }

    val inv = 1.0 / (times - 1)
    val stepX = (toX - fromX) * inv
    val stepY = (toY - fromY) * inv
    val stepZ = (toZ - fromZ) * inv

    var currX = fromX
    var currY = fromY
    var currZ = fromZ

    for (i in 0 until times) {
        task(i, currX, currY, currZ)
        currX += stepX
        currY += stepY
        currZ += stepZ
    }
}