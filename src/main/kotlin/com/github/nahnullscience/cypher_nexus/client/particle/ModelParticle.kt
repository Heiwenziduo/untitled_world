package com.github.nahnullscience.cypher_nexus.client.particle

import com.github.nahnullscience.cypher_nexus.client.renderer.state.CypherTrailParticleRenderState
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.client.model.Model
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.particle.Particle
import net.minecraft.client.particle.ParticleProvider
import net.minecraft.client.particle.SpriteSet
import net.minecraft.client.renderer.Sheets
import net.minecraft.client.renderer.blockentity.StandingSignRenderer
import net.minecraft.client.resources.model.sprite.SpriteId
import net.minecraft.core.particles.SimpleParticleType
import net.minecraft.util.RandomSource
import net.minecraft.world.level.block.PlainSignBlock
import net.minecraft.world.level.block.state.properties.WoodType

class ModelParticle(
    level: ClientLevel,
    x: Double, y: Double, z: Double,
    xa: Double, ya: Double, za: Double
) : CypherTrailParticle(level, x, y, z, xa, ya, za) {

    val model: Model.Simple = StandingSignRenderer.createSignModel(
        Minecraft.getInstance().entityModels,
        WoodType.OAK,
        PlainSignBlock.Attachment.GROUND
    )
    val sprite: SpriteId = Sheets.getSignSprite(WoodType.OAK)

    override fun extract(state: CypherTrailParticleRenderState, camera: Camera, partialTickTime: Float) {
        super.extract(state, camera, partialTickTime)
    }

    class TrailProvider(val spriteSet: SpriteSet) : ParticleProvider<SimpleParticleType> {
        override fun createParticle(
            options: SimpleParticleType,
            level: ClientLevel,
            x: Double,
            y: Double,
            z: Double,
            xAux: Double,
            yAux: Double,
            zAux: Double,
            random: RandomSource
        ): Particle {
            return ModelParticle(level, x, y, z, xAux, yAux, zAux)
        }
    }
}