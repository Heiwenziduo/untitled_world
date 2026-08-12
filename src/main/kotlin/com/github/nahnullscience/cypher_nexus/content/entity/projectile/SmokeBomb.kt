package com.github.nahnullscience.cypher_nexus.content.entity.projectile

import com.github.nahnullscience.cypher_nexus.init.mod.Cyphers
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractDedicatedCypherProjectile
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ExplosionSettings
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.Level

class SmokeBomb(
    entityType: EntityType<out AbstractDedicatedCypherProjectile>,
    level: Level
) : AbstractDedicatedCypherProjectile(entityType, level) {
    override val cypherHolder = Cyphers.SMOKE_BOMB

    override fun initExplosion(): ExplosionSettings<*> {
        val smoke = ExplosionSettings(this)
        with(smoke) {
            radiusSqr = 1f
//            smallParticle = ParticleTypes.CAMPFIRE_SIGNAL_SMOKE
//            largeParticle = ParticleTypes.CAMPFIRE_SIGNAL_SMOKE
            sound = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.SMOKER_SMOKE)
        }
        return smoke
    }
}