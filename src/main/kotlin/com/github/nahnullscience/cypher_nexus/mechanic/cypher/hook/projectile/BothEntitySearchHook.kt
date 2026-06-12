package com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractCypherProjectile
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookModule
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookModule.HookType.PROJECTILE
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level

interface BothEntitySearchHook {
    fun entitySearchBoth(level: Level, projectile: AbstractCypherProjectile, strength: Int, target: Entity)
    fun needSearch(level: Level, projectile: AbstractCypherProjectile): Boolean
    companion object {
        val MODULE = HookModule(
            CypherNexus.modResource("entity_search"),
            BothEntitySearchHook::class,
            true,
            PROJECTILE,
            false
        )
    }
}