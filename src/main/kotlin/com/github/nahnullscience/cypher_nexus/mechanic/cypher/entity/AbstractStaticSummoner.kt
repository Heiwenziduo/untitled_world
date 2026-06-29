package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity

import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.Level

/**
 * base class for cyphers those who functions as a summoner,
 * fixed existing, generally immobile and invisible, and will summon something in the end
 * */
abstract class AbstractStaticSummoner(
    entityType: EntityType<out DedicatedCypherProjectile>,
    level: Level
) : DedicatedCypherProjectile(entityType, level) {

    override var existing: Int = 2

    override fun beforeDiscardBoth(reason: DiscardReason) {
        summon()
        super.beforeDiscardBoth(reason)
    }

    abstract fun summon()
}