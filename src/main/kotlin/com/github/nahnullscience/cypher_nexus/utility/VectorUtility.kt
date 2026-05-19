package com.github.nahnullscience.cypher_nexus.utility

import net.minecraft.core.Direction
import net.minecraft.core.Vec3i
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import kotlin.math.abs


object VectorUtility {
    fun toVec3i(v: Vec3) = Vec3i(v.x.toInt(), v.y.toInt(), v.z.toInt())

    fun getDireFromHit(hitPoint: Vec3?, aabb: AABB): Direction? {
        if (hitPoint == null) return null
        val epsilon = 1e-5
        return when {
            abs(hitPoint.x - aabb.minX) < epsilon -> Direction.WEST
            abs(hitPoint.x - aabb.maxX) < epsilon -> Direction.EAST
            abs(hitPoint.y - aabb.minY) < epsilon -> Direction.DOWN
            abs(hitPoint.y - aabb.maxY) < epsilon -> Direction.UP
            abs(hitPoint.z - aabb.minZ) < epsilon -> Direction.NORTH
            abs(hitPoint.z - aabb.maxZ) < epsilon -> Direction.SOUTH
            else -> null
        }
    }

    /**
     * @return new V3 xyz value same as "from", symbol same as "to"
     * */
    fun toSameDire(from: Vec3, to: Vec3): Vec3 {
        return Vec3(
            MathUtility.toSameSymbol(from.x, to.x),
            MathUtility.toSameSymbol(from.y, to.y),
            MathUtility.toSameSymbol(from.z, to.z)
        )
    }
}

