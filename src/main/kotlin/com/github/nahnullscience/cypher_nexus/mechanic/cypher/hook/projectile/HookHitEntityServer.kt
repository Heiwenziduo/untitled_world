package com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile

import com.github.nahnullscience.cypher_nexus.content.entity.AbstractCypherProjectile
import net.minecraft.world.level.Level
import net.minecraft.world.phys.HitResult

interface HookHitEntityServer {
    fun onHitServer(level: Level, projectile: AbstractCypherProjectile, strength: Int, result: HitResult)
}