package com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookModule
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3

interface BothOnBounceHook {
    fun <CY> onBounceBoth(level: Level, projectile: CY, strength: Int, bounceCount: Int, bouncePoint: Vec3) where CY : Entity, CY : ICypherEntity
    companion object {
        val HOOK = HookModule.HookBuilder("bounce", BothOnBounceHook::class)
    }
}