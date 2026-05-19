package com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile

import com.github.nahnullscience.cypher_nexus.content.entity.CypherProjectile
import net.minecraft.world.level.Level

interface FirstTickHook {
    fun firstTickBoth(level: Level, projectile: CypherProjectile, strength: Int)
}