package com.github.nahnullscience.cypher_nexus.content.entity.statics

import com.github.nahnullscience.cypher_nexus.init.mod.Cyphers.EXPLOSION
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractDedicatedCypherProjectile
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractStaticSummoner
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.Explosion
import net.minecraft.world.level.Level

class SummonedExplosion (
    entityType: EntityType<out AbstractDedicatedCypherProjectile>,
    level: Level
) : AbstractStaticSummoner(entityType, level) {

    override val cypherHolder = EXPLOSION
    override fun summon() {
        if (!level().isClientSide) {
            // check  net.minecraft.world.level.ExplosionDamageCalculator  &&  Explosion.BlockInteraction
            level().explode(
                this,
                Explosion.getDefaultDamageSource(level(), this),
                null,
                x,
                y,
                z,
                4.0f,
                false,
                Level.ExplosionInteraction.TNT
            )
        }
    }
}