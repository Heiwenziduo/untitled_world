package com.github.nahnullscience.cypher_nexus.utility

import net.minecraft.core.Direction
import net.minecraft.core.Direction.Axis
import net.minecraft.world.phys.Vec3
import org.joml.Quaternionf
import org.joml.Vector3d

/**
 * assume [front] & [left] are normalized
 *
 * */
class CoordinateDefinition(
    val front: Vec3,
    val left: Vec3,
    up: Vec3? = null
) {
    val up = up ?: front.cross(left)
    init {

    }
    constructor(front: Direction, left: Direction): this(front.unitVec3, left.unitVec3)


    operator fun component1() = front
    operator fun component2() = left
    operator fun component3() = up

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
        fun fromFrontUp(front: Vec3, up: Vec3): CoordinateDefinition {
            val left = up.cross(front)
            return CoordinateDefinition(front, left, up)
        }

        /**
         *
         * */
        inline fun faceDirectionWithUpVector(
            direction: Direction,
            approximateUp: Vec3,
            fallback: () -> Vec3
        ): CoordinateDefinition {
            val v3d = Vector3d()
            when(direction.axis) {
                Axis.X -> { v3d.y = approximateUp.y; v3d.z = approximateUp.z }
                Axis.Y -> { v3d.x = approximateUp.x; v3d.z = approximateUp.z }
                Axis.Z -> { v3d.x = approximateUp.x; v3d.y = approximateUp.y }
            }
            val up =
                if (v3d.lengthSquared() > 1e-6) v3d.normalize().toVec3()
                else fallback() // if speed vector and direction are in the same direction

            return fromFrontUp(direction.unitVec3, up)
        }
    }
}