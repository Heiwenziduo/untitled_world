package com.github.nahnullscience.cypher_nexus.utility

import net.minecraft.world.phys.Vec3
import org.joml.Quaternionf

/**
 * assume [front] & [left] are normalized
 *
 * */
data class CoordinateDefinition(
    val front: Vec3,
    val left: Vec3
) {
    var up = front.cross(left)
        private set

    init {

    }

    val reX get() = left
    val reY get() = up
    val reZ get() = front

    fun down() = up.reverse()
    fun right() = left.reverse()
    fun back() = front.reverse()

    @PublishedApi
    internal var patternCache: Array<PosDirePair?>? = null
    inline fun getOrComputePatternCache(number: Int, compute: () -> PosDirePair): PosDirePair {
        val number = number and 0b0111 // cache 8 vector may be enough
        val cache = patternCache ?: arrayOfNulls<PosDirePair>(8).also { patternCache = it }
        return cache[number] ?: compute().also { cache[number] = it }
    }

    fun rightScrewFromTop(dire: Vec3, rad: Float): Vec3 {
        val top = up.toVector3f()
        Quaternionf().rotationAxis(rad, top).let {
            return dire.toVector3f().rotate(it).toVec3()
        }
    }

    companion object {
    }
}