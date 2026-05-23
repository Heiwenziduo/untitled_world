package com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.content.entity.AbstractCypherProjectile
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookModule
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookModule.HookType.PROJECTILE
import net.minecraft.world.level.Level

interface BothFirstTickHook {
    fun firstTickBoth(level: Level, projectile: AbstractCypherProjectile, strength: Int)

    companion object {
        val MODULE = HookModule(
            CypherNexus.modResource("first_tick"),
            BothFirstTickHook::class,
            true,
            PROJECTILE,
            false
        )
    }
}