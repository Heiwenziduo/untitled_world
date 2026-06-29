package com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookModule
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraft.world.phys.HitResult

interface BothHitEntityHook {
    fun <CY> onHitBoth(level: Level, projectile: CY, strength: Int, result: HitResult) where CY : Entity, CY : ICypherEntity
    companion object {
        val HOOK = HookModule.HookBuilder("hit_entity", BothHitEntityHook::class)
    }
}