package com.github.nahnullscience.cypher_nexus.utility

import net.minecraft.world.phys.Vec3
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.PI
import kotlin.math.sqrt

//fun <T> T.toSameSymbol(s: T): T where T : Number, T : Comparable<T> {
//    if (this == 0 || s == 0) return this
//    return if (this * s > 0) this else this * -1
//}
@PublishedApi
internal val emptyDoubleArray = doubleArrayOf()

const val RAD_2_ANG = 180.0 / PI
const val RAD_2_ANG_F = RAD_2_ANG.toFloat()
const val ANG_2_RAD = PI / 180.0
const val ANG_2_RAD_F = ANG_2_RAD.toFloat()

fun Double.ang2Rad() = Math.toRadians(this)
fun Double.rad2Ang() = Math.toDegrees(this)

fun Float.ang2Rad() = this * ANG_2_RAD_F
fun Float.rad2Ang() = this * RAD_2_ANG_F

/**  */
fun Double.toSameSymbol(t: Double): Double {
    if (this == 0.0 || t == 0.0) return this
    return if (this * t > 0) this else this * -1
}

inline fun Double.finiteOrDefault(default: () -> Double): Double {
    return if (this.isFinite()) this
    else default()
}

inline fun Float.finiteOrDefault(default: () -> Float): Float {
    return if (this.isFinite()) this
    else default()
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

fun Int.showBits32(chunkSize: Int = 4): String {
    val bits = this
        .toUInt()
        .toString(radix = 2)
        .padStart(32, '0')
        .chunked(chunkSize)
        .joinToString(" ")
    return bits
}

fun Long.showBits64(chunkSize: Int = 4): String {
    val bits = this
        .toULong()
        .toString(radix = 2)
        .padStart(64, '0')
        .chunked(chunkSize)
        .joinToString(" ")
    return bits
}

/**
 * execute [task] given times on points evenly distributed through 'start' to 'end'
 * @see [forEachGap]
 * */
inline fun forEachBetween(
    startX: Double, startY: Double, startZ: Double,
    endX: Double, endY: Double, endZ: Double,
    times: Int,
    task: (step: Int, x: Double, y: Double, z: Double) -> Unit
) {
    if (times <= 1) {
        task(0, startX, startY, startZ)
        return
    }

    val inv = 1.0 / (times - 1)
    val stepX = (endX - startX) * inv
    val stepY = (endY - startY) * inv
    val stepZ = (endZ - startZ) * inv

    var currX = startX
    var currY = startY
    var currZ = startZ

    for (i in 0 until times) {
        task(i, currX, currY, currZ)
        currX += stepX
        currY += stepY
        currZ += stepZ
    }
}

/**
 * execute [task] on points within `start` to `end` in an interval of given gap.
 * e.g. once a gap distance has been traveled
 * @see [forEachBetween]
 * */
inline fun forEachGap(
    startX: Double, startY: Double, startZ: Double,
    endX: Double, endY: Double, endZ: Double,
    gap: Double,
    atLeastOnce: Boolean = true,
    task: (step: Int, x: Double, y: Double, z: Double) -> Unit,
) {
    if (gap <= 0.0) {
        if (atLeastOnce) task(0, startX, startY, startZ)
        return
    }

    val dx = endX - startX
    val dy = endY - startY
    val dz = endZ - startZ
    val distSq = dx * dx + dy * dy + dz * dz

    if (distSq < gap * gap) {
        if (atLeastOnce) task(0, startX, startY, startZ)
        return
    }

    val dist = sqrt(distSq)
    val stepFactor = gap / dist
    val stepX = dx * stepFactor
    val stepY = dy * stepFactor
    val stepZ = dz * stepFactor

    val steps = (dist / gap).toInt()
    var currX = startX
    var currY = startY
    var currZ = startZ

    for (i in 0..steps) {
        task(i, currX, currY, currZ)
        currX += stepX
        currY += stepY
        currZ += stepZ
    }
}
