package com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookModule.HookBuilder
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.IHook
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level

interface OnExplodeHook : IHook {
    /** does not contain ERASE */
    fun <CE> onExplode(
        index: Int,
        count: Int,
        level: Level,
        cyEntity: CE,
    ) where CE : Entity, CE : ICypherEntity

    companion object {
        val HOOK = HookBuilder("on_explode", OnExplodeHook::class)
    }
}