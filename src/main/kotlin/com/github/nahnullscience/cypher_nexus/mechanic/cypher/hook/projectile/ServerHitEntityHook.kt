package com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.content.entity.AbstractCypherProjectile
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookModule
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookModule.HookType.PROJECTILE
import net.minecraft.world.level.Level
import net.minecraft.world.phys.HitResult

interface ServerHitEntityHook {
    fun onHitServer(level: Level, projectile: AbstractCypherProjectile, strength: Int, result: HitResult)
    companion object {
        val MODULE = HookModule(
            CypherNexus.modResource("hit_entity"),
            ServerHitEntityHook::class,
            false,
            PROJECTILE,
            false
        )
    }
}