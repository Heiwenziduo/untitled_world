package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.steerer

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.DiscardReason
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity
import com.github.nahnullscience.cypher_nexus.utility.i.IRegisterable
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.Entity

/**
 * stateless behavior modules.
 * */
abstract class AbstractCypherSteerer(
    override val resource: Identifier
) : IRegisterable {
    /**
     * on both sides
     * */
    abstract fun <CE> init(ce: CE) where CE : ICypherEntity, CE : Entity
    /**
     * on both sides
     * */
    abstract fun <CE> tick(ce: CE) where CE : ICypherEntity, CE : Entity
    /**
     * on both sides
     * */
    abstract fun <CE> tickSpeedOverride(ce: CE) where CE : ICypherEntity, CE : Entity
    /**
     * server only
     * */
    abstract fun <CE> discard(ce: CE, reason: DiscardReason) where CE : ICypherEntity, CE : Entity
}