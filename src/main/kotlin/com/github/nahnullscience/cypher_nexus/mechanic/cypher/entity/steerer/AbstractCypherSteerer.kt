package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.steerer

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.DiscardReason
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation.ICypherEntity
import com.github.nahnullscience.cypher_nexus.utility.i.IRegisterable
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.Entity

abstract class AbstractCypherSteerer(
    override val resource: Identifier
) : IRegisterable {
    abstract fun <CE> init(ce: CE) where CE : ICypherEntity, CE : Entity
    abstract fun <CE> tick(ce: CE) where CE : ICypherEntity, CE : Entity
    abstract fun <CE> tickSpeedOverride(ce: CE) where CE : ICypherEntity, CE : Entity
    abstract fun <CE> discard(ce: CE, reason: DiscardReason) where CE : ICypherEntity, CE : Entity
}