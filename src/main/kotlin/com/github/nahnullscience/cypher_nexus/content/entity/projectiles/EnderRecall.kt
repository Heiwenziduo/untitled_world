package com.github.nahnullscience.cypher_nexus.content.entity.projectiles

import com.github.nahnullscience.cypher_nexus.init.mod.Cyphers.ENDER_RECALL
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractDedicatedCypherProjectile
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.Level

class EnderRecall(
    entityType: EntityType<out AbstractDedicatedCypherProjectile>,
    level: Level
) : EnderTeleportation(entityType, level) {
    override val cypherHolder = ENDER_RECALL
}