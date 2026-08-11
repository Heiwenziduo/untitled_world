package com.github.nahnullscience.cypher_nexus.client.renderer.cypher.projectile

import com.github.nahnullscience.cypher_nexus.client.particle.addCypherTrailParticle
import com.github.nahnullscience.cypher_nexus.client.renderer.cypher.SimpleItemProjectileRenderer
import com.github.nahnullscience.cypher_nexus.content.entity.projectile.Pinky
import com.github.nahnullscience.cypher_nexus.utility.getArrayRGB
import com.github.nahnullscience.cypher_nexus.utility.linearInterpolateGaps
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.item.Items
import java.awt.Color

class PinkyCypherRenderer(
    context: Context
) : SimpleItemProjectileRenderer<Pinky>(context, Items.SLIME_BALL) {
    override fun addTrailParticles(
        level: ClientLevel,
        entity: Pinky,
        x: Double,
        y: Double,
        z: Double,
        xo: Double,
        yo: Double,
        zo: Double
    ) {
        val speed = entity.deltaMovement
        linearInterpolateGaps(xo, yo, zo, x, y, z, 0.25) { step, x, y, z ->
            addCypherTrailParticle(
                ParticleTypes.CLOUD,
                x, y, z,
                -speed.x * 0.25,
                -speed.y * 0.25,
                -speed.z * 0.25
            ) {
                setColor(pinkF[0], pinkF[1], pinkF[2])
            }
        }
    }

    companion object {
        private val pink = Color(0xFB70B5)
        private val pinkF = pink.getArrayRGB()
    }
}