package com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookModule
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.IHook
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraft.world.phys.HitResult

interface HitEntityHook : IHook {
    fun <CE> onHit(
        index: Int,
        count: Int,
        level: Level,
        cyEntity: CE,
        result: HitResult
    ) where CE : Entity, CE : ICypherEntity

    companion object {
        val HOOK = HookModule.HookBuilder("hit_entity", HitEntityHook::class)
    }
}