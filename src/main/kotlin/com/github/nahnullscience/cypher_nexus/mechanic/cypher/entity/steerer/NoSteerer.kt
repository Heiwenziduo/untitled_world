package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.steerer

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.DiscardReason
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity
import net.minecraft.world.entity.Entity

object NoSteerer : AbstractCypherSteerer(
    CypherNexus.modResource("no_steerer")
) {
    override fun <CE> init(ce: CE) where CE : ICypherEntity, CE : Entity = Unit

    override fun <CE> tick(ce: CE) where CE : ICypherEntity, CE : Entity = Unit

    override fun <CE> tickSpeedOverride(ce: CE) where CE : ICypherEntity, CE : Entity = Unit

    override fun <CE> discard(ce: CE, reason: DiscardReason) where CE : ICypherEntity, CE : Entity = Unit
}