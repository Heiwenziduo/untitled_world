package com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.invoking

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookModule
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.IHook
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotState.ShotStateAccessor
import com.github.nahnullscience.cypher_nexus.utility.linear_space.AnchoredCoordinate
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity

interface ServerInvokeSurroundingCaptureHook : IHook {

    fun forEntityCapturedServer(
        index: Int,
        count: Int,
        level: ServerLevel,
        owner: Entity?,
        state: ShotStateAccessor,
        coordinate: AnchoredCoordinate,
        captured: Entity
    )

    companion object {
        val HOOK = HookModule.HookBuilder("invoke_surround_capture", ServerInvokeSurroundingCaptureHook::class).invoking()
    }
}