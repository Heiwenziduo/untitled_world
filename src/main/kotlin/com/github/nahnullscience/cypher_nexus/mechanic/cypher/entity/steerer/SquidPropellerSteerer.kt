package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.steerer

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.DiscardReason
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.Entity

/**
 * makes a projectile periodically phase between decelerating and accelerating.
 * */
open class SquidPropellerSteerer(resource: Identifier) : AbstractCypherSteerer(resource) {
    override fun <CE> init(ce: CE) where CE : ICypherEntity, CE : Entity {
    }

    override fun <CE> tick(ce: CE) where CE : ICypherEntity, CE : Entity {
    }

    override fun <CE> tickSpeedOverride(ce: CE) where CE : ICypherEntity, CE : Entity {
    }

    override fun <CE> discard(
        ce: CE,
        reason: DiscardReason
    ) where CE : ICypherEntity, CE : Entity {
    }
}
