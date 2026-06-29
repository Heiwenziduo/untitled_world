package com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookModule
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level

interface BothFirstTickHook {
    fun <CY> firstTickBoth(level: Level, projectile: CY, strength: Int) where CY : Entity, CY : ICypherEntity

    companion object {
        val HOOK = HookModule.HookBuilder("first_tick", BothFirstTickHook::class)
    }
}