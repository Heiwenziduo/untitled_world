package com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile

import com.github.nahnullscience.cypher_nexus.content.entity.AbstractCypherProjectile
import net.minecraft.world.level.Level

interface HookBeforeDiscardBoth {
    /** does not contain ERASE */
    fun beforeDiscardBoth(level: Level, projectile: AbstractCypherProjectile, strength: Int, reason: AbstractCypherProjectile.DiscardReason)
}