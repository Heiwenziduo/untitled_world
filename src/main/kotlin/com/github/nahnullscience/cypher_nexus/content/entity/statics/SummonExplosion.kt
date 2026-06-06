package com.github.nahnullscience.cypher_nexus.content.entity.statics

import com.github.nahnullscience.cypher_nexus.content.cypher.static_projectile.ExplosionCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractCypherProjectile
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractStaticSummoner
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.Level

class SummonExplosion (
    entityType: EntityType<out AbstractCypherProjectile>,
    level: Level
) : AbstractStaticSummoner(entityType, level) {
    override val cypher = ExplosionCypher

}