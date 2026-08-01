package com.github.nahnullscience.cypher_nexus.utility

import net.minecraft.world.phys.Vec3

/**
 * assume [front] & [left] are normalized
 *
 * */
data class CoordinateDefinition(
    val front: Vec3,
    val left: Vec3
) {
    val top = front.cross(left)

    init {

    }

    val x get() = left
    val y get() = top
    val z get() = front

    @PublishedApi
    internal var patternCache: Array<PosDirePair?>? = null
    inline fun getOrComputePatternCache(number: Int, compute: () -> PosDirePair): PosDirePair {
        val number = number and 0x0111 // cache 8 vector may be enough
        val cache = patternCache ?: arrayOfNulls<PosDirePair>(8).also { patternCache = it }
        return cache[number] ?: compute().also { cache[number] = it }
    }
}