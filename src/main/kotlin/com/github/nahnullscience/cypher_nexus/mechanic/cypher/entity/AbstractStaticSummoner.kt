package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity

import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity
import net.minecraft.world.entity.Entity
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

    override fun doEntitySetup() {
        setAttribute(CypherAttributes.EXISTING, 2.0)
    }

    override fun <CE> beforeDiscardServer(ce: CE, reason: DiscardReason) where CE : Entity, CE : ICypherEntity {
        summon()
        super.beforeDiscardServer(ce, reason)
    }

    abstract fun summon()
}