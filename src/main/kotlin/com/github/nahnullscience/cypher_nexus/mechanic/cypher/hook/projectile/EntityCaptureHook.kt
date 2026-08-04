package com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookModule
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.IHook
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level

interface EntityCaptureHook : IHook {
    /**
     * O(m * n)
     * */
    fun <CE> forEntityCaptured(
        index: Int,
        count: Int,
        level: Level,
        cyEntity: CE,
        target: Entity
    ) where CE : Entity, CE : ICypherEntity

    fun <CE> needCapture(level: Level, cyEntity: CE): Boolean where CE : Entity, CE : ICypherEntity

    companion object {
        val HOOK = HookModule.HookBuilder("entity_search", EntityCaptureHook::class)
    }
}