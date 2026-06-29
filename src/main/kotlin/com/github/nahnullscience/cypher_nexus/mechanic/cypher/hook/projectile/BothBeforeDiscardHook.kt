package com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.DiscardReason
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookModule
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level

interface BothBeforeDiscardHook {
    /** does not contain ERASE */
    fun <CY> beforeDiscardBoth(level: Level, projectile: CY, strength: Int, reason: DiscardReason) where CY : Entity, CY : ICypherEntity

    companion object {
        val HOOK = HookModule.HookBuilder("before_discard", BothBeforeDiscardHook::class)
    }
}