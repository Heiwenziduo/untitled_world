package com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookModule
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.IHook
import net.minecraft.core.Direction
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3

interface OnBounceHook : IHook {
    fun <CE> onBounce(
        index: Int,
        count: Int,
        level: Level,
        cyEntity: CE,
        bounceCount: Int,
        bounceSurface: Direction,
        bouncePoint: Vec3
    ) where CE : Entity, CE : ICypherEntity

    companion object {
        val HOOK = HookModule.HookBuilder("on_bounce", OnBounceHook::class)
    }
}