package com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractCypherProjectile
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookModule
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookModule.HookType.PROJECTILE
import net.minecraft.world.level.Level

interface BothBeforeDiscardHook {
    /** does not contain ERASE */
    fun beforeDiscardBoth(level: Level, projectile: AbstractCypherProjectile, strength: Int, reason: AbstractCypherProjectile.DiscardReason)

    companion object {
        val MODULE = HookModule(
            CypherNexus.modResource("before_discard"),
            BothBeforeDiscardHook::class,
            true,
            PROJECTILE,
            false
        )
    }
}