package com.github.nahnullscience.cypher_nexus.content.entity.statics

import com.github.nahnullscience.cypher_nexus.init.mod.Cyphers.LIGHTING
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.DedicatedCypherProjectile
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractStaticSummoner
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.EntitySpawnReason
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.Level

class SummonedLightning (
    entityType: EntityType<out DedicatedCypherProjectile>,
    level: Level
) : AbstractStaticSummoner(entityType, level) {

    override val cypherHolder = LIGHTING
    override fun summon() {
        if (level() is ServerLevel) {
            EntityType.LIGHTNING_BOLT.create(level(), EntitySpawnReason.MOB_SUMMONED)?.let { bolt ->
                bolt.setVisualOnly(true)
                bolt.snapTo(position())
                level().addFreshEntity(bolt)
            }
        }
    }
}