package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity

import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.Level

abstract class AbstractStaticSummoner(
    entityType: EntityType<out AbstractCypherProjectile>,
    level: Level
) : AbstractCypherProjectile(entityType, level) {

    abstract override var existing: Int

    abstract override fun onBeforeDiscardBoth(reason: DiscardReason)
}