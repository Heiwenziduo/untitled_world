package com.github.nahnullscience.cypher_nexus.utility.linear_space

import com.github.nahnullscience.cypher_nexus.utility.minus
import com.github.nahnullscience.cypher_nexus.utility.times
import net.minecraft.world.phys.Vec3
import kotlin.math.sign

/**
 * define a plane by the given [pos] and [normal], assume the normal is normalized.
 *
 * define the direction the [normal] is pointing at is the `positive`, the invert direction is `negative`.
 * */
data class PlaneDefinition(
    val normal: Vec3,
    val pos: Vec3
) {
    val unit get() = normal
    companion object {
        /**
         * @return 1 if at positive side, -1 if negative, or 0 if on the plane
         * */
        fun Vec3.sideOf(plane: PlaneDefinition): Int {
            val v = plane.pos.vectorTo(this)
            val dot = v.dot(plane.normal)
            return sign(dot).toInt()
        }
        /**
         * @return the perpendicular displacement vector pointing from the given V3 to the plane.
         * The magnitude of this vector is the exact distance to the plane.
         */
        fun Vec3.vectorTo(plane: PlaneDefinition): Vec3 {
            val v = plane.pos.vectorTo(this)
            val dot = v.dot(plane.normal)
            return plane.normal.scale(-dot)
        }

        /**
         * @return the perpendicular vector pointing from the given V3 to the nearest point on the line of [PlaneDefinition.normal].
         * The magnitude of the returned vector is the exact shortest distance to the line.
         */
        fun Vec3.vectorToPlaneNormal(plane: PlaneDefinition): Vec3 {
            val v = plane.pos.vectorTo(this)
            val dot = v.dot(plane.normal)
            val along = plane.normal * dot
            return along - v
        }
    }
}