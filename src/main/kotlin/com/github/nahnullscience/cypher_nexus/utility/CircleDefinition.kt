package com.github.nahnullscience.cypher_nexus.utility

import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f

/**
 * rotation of the circle follows right-hand screw law
 * @param center
 * @param radius start from `ZERO`
 * @param normal surface normal, should be `normalized`
 * */
data class CircleDefinition(
    val center: Vec3,
    val radius: Vec3,
    val normal: Vector3f
) {
    companion object {
        fun fromEntityEye(entity: Entity, endpoint: Vec3, normal: Vector3f, maxRadiusLength: Double = -1.0): CircleDefinition {
            val center = entity.eyePosition
            var radius = center.vectorTo(endpoint)
            if (maxRadiusLength > 0) radius = radius.coerceMaxLength(maxRadiusLength)
            return CircleDefinition(center, radius, normal)
        }
    }
}