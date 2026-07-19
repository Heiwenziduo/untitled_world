package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity

import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.Level

/**
 * base class for cyphers those who functions as a summoner,
 * fixed existing, generally immobile and invisible, and will summon something in the end
 * */
abstract class AbstractStaticSummoner(
    entityType: EntityType<out AbstractDedicatedCypherProjectile>,
    level: Level
) : AbstractDedicatedCypherProjectile(entityType, level) {

    override fun getExisting() = 2

    override fun beforeDiscard(reason: DiscardReason) {
        summon()
        super.beforeDiscard(reason)
    }

    abstract fun summon()
}