package com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookModule
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.IHook
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level

interface TickMovementFinalizeHook : IHook {
    /**
     *
     * */
    fun <CE> finalizeTickMovement(
        index: Int,
        count: Int,
        level: Level,
        cyEntity: CE
    ) where CE : Entity, CE : ICypherEntity

    companion object {
        val HOOK = HookModule.HookBuilder("finalize_tick_movement", TickMovementFinalizeHook::class)
    }
}