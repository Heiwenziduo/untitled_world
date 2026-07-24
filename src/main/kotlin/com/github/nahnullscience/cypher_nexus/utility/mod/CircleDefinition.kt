package com.github.nahnullscience.cypher_nexus.utility.mod

import com.github.nahnullscience.cypher_nexus.utility.coerceMaxLength
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f

/**
 * rotation of the circle follows right-hand screw law
 * */
data class CircleDefinition(
    val center: Vec3,
    val radius: Vec3,
    val normal: Vector3f
) {
    companion object {
        fun fromEntityEye(entity: Entity, endpoint: Vec3, normal: Vector3f, maxRadius: Double = -1.0): CircleDefinition {
            val center = entity.eyePosition
            var radius = center.vectorTo(endpoint)
            if (maxRadius > 0) radius = radius.coerceMaxLength(maxRadius)
            return CircleDefinition(center, radius, normal)
        }
    }
}