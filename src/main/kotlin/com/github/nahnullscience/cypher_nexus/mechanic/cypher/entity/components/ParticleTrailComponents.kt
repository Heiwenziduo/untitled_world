package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components

import net.minecraft.core.particles.ParticleOptions

enum class TrailInterpolation {
    TIME,
    GAP
}

// TODO let modifiers can add trails
data class ParticleTrailSetting(
    val particleOption: ParticleOptions,
    val interpolation: TrailInterpolation
) {
//    inline fun speedThen(p: () -> Unit) {}
}