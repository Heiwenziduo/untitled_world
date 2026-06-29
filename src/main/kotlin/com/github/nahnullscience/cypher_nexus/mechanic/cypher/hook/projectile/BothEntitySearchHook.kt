package com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookModule
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level

interface BothEntitySearchHook {
    fun <CY> entitySearchBoth(level: Level, projectile: CY, strength: Int, target: Entity) where CY : Entity, CY : ICypherEntity
    fun <CY> needSearch(level: Level, projectile: CY): Boolean where CY : Entity, CY : ICypherEntity
    companion object {
        val HOOK = HookModule.HookBuilder("entity_search", BothEntitySearchHook::class)
    }
}