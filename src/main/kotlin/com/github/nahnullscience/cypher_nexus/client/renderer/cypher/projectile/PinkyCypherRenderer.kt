package com.github.nahnullscience.cypher_nexus.client.renderer.cypher.projectile

import com.github.nahnullscience.cypher_nexus.client.renderer.cypher.SimpleItemProjectileRenderer
import com.github.nahnullscience.cypher_nexus.client.util.addCypherTrailParticle
import com.github.nahnullscience.cypher_nexus.content.entity.projectile.Pinky
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.BouncePointsManager.Companion.forEachGap
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntityAttributeAccessor.Companion.getEffectRadius
import com.github.nahnullscience.cypher_nexus.utility.getArrayRGB
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
        ce: Pinky,
        x: Double,
        y: Double,
        z: Double,
        xo: Double,
        yo: Double,
        zo: Double
    ) {
        val speed = ce.deltaMovement
        val scale = ce.getEffectRadius().coerceIn(0.25f, 4f) * 0.5f
        forEachGap(
            xo, yo, zo,
            x, y, z,
            0.25,
            ce.bouncePoints,
        ) { step, x, y, z ->
            addCypherTrailParticle(
                ParticleTypes.CLOUD,
                x, y, z,
                -speed.x * 0.25,
                -speed.y * 0.25,
                -speed.z * 0.25
            ) {
                lifetime = 15
                scale(scale)
                if (ce.dyed) ce.hueFloatArray.let {
                    setColor(it[0], it[1], it[2])
                    setAlpha(it[3])
                } else setColor(pinkF[0], pinkF[1], pinkF[2])
            }
        }
    }

    companion object {
        private val pink = Color(0xFB70B5)
        private val pinkF = pink.getArrayRGB()
    }
}
