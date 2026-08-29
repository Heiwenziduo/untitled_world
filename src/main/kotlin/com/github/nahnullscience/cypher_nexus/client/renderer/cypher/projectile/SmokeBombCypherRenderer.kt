package com.github.nahnullscience.cypher_nexus.client.renderer.cypher.projectile

import com.github.nahnullscience.cypher_nexus.client.renderer.cypher.SimpleParticleProjectileRenderer
import com.github.nahnullscience.cypher_nexus.client.util.addCypherTrailParticle
import com.github.nahnullscience.cypher_nexus.content.entity.projectile.SmokeBomb
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.BouncePointsManager.Companion.forEachGap
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context
import net.minecraft.core.particles.ParticleTypes

class SmokeBombCypherRenderer(context: Context) : SimpleParticleProjectileRenderer<SmokeBomb>(context) {

    override fun addTrailParticles(
        level: ClientLevel,
        ce: SmokeBomb,
        x: Double, y: Double, z: Double,
        xo: Double, yo: Double, zo: Double
    ) {
        forEachGap(
            xo, yo, zo,
            x, y, z,
            0.33,
            ce.bouncePoints,
        ) { step, x, y, z ->
            addCypherTrailParticle(
                ce,
                ParticleTypes.CAMPFIRE_COSY_SMOKE,
                x, y, z,
            ) {
                lifetime += 60
            }
        }
    }
}
