package com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile

import com.github.nahnullscience.cypher_nexus.content.entity.CypherProjectile
import net.minecraft.world.level.Level

interface TickBehaviorHook {
    /** call on both side, before projectile-tick (which perform bounce or hit logic) */
    fun tickBehaviorBoth(level: Level, projectile: CypherProjectile, strength: Int)
}