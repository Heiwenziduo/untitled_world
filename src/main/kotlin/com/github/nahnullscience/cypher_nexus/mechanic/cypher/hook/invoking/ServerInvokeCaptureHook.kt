package com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.invoking

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookModule
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk.StateAccessor
import com.github.nahnullscience.cypher_nexus.utility.mod.PosDirePair
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity

interface ServerInvokeCaptureHook {

    fun forEntityCaptured(
        level: ServerLevel,
        captured: Entity,
        strength: Int,
        pair: PosDirePair,
        state: StateAccessor,
        index: Int,
    )

    companion object {
        val HOOK = HookModule.HookBuilder("invoke_entity_capture", ServerInvokeCaptureHook::class).invoking()
    }
}