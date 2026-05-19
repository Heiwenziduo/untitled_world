package com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile

import com.github.nahnullscience.cypher_nexus.content.entity.CypherProjectile
import net.minecraft.world.level.Level

interface BeforeDiscardHook {
    /** does not contain ERASE */
    fun beforeDiscardBoth(level: Level, projectile: CypherProjectile, strength: Int, reason: CypherProjectile.DiscardReason)
}