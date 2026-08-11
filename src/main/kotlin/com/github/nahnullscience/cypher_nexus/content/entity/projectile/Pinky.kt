package com.github.nahnullscience.cypher_nexus.content.entity.projectile

import com.github.nahnullscience.cypher_nexus.init.mod.Cyphers
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractDedicatedCypherProjectile
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.Level

class Pinky(
    entityType: EntityType<out AbstractDedicatedCypherProjectile>,
    level: Level
) : AbstractDedicatedCypherProjectile(entityType, level) {
    override val cypherHolder = Cyphers.PINKY
    override fun getBounceSpeedDegrade(): Double = 1.0
}