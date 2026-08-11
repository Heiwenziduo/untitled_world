package com.github.nahnullscience.cypher_nexus.content.entity.utility

import com.github.nahnullscience.cypher_nexus.init.mod.Cyphers.LONG_DISTANCE_PROJECTION
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractDedicatedCypherProjectile
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.Level

open class DistanceDeliverer(
    entityType: EntityType<out AbstractDedicatedCypherProjectile>,
    level: Level
) : AbstractDedicatedCypherProjectile(entityType, level) {
    override val cypherHolder = LONG_DISTANCE_PROJECTION

    override fun displayFireAnimation(): Boolean = false
}