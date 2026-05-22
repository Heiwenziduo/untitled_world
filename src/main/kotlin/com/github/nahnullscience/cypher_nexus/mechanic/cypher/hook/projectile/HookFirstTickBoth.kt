package com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile

import com.github.nahnullscience.cypher_nexus.content.entity.AbstractCypherProjectile
import net.minecraft.world.level.Level

interface HookFirstTickBoth {
    fun firstTickBoth(level: Level, projectile: AbstractCypherProjectile, strength: Int)
}