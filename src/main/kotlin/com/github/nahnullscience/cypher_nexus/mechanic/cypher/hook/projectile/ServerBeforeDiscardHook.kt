package com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.DiscardReason
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookModule
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.IHook
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level

interface ServerBeforeDiscardHook : IHook {
    /** does not contain ERASE */
    fun <CE> beforeDiscardServer(
        index: Int,
        count: Int,
        level: Level,
        cyEntity: CE,
        reason: DiscardReason
    ) where CE : Entity, CE : ICypherEntity

    companion object {
        val HOOK = HookModule.HookBuilder("before_discard", ServerBeforeDiscardHook::class)
    }
}