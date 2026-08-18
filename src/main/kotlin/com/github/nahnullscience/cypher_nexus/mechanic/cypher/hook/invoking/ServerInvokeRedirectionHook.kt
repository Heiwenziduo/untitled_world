package com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.invoking

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookModule
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.IHook
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotState.ShotStateViewer
import com.github.nahnullscience.cypher_nexus.utility.linear_space.AnchoredCoordinate
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity

interface ServerInvokeRedirectionHook : IHook {

    fun invokeRedirectServer(
        index: Int,
        count: Int,
        level: ServerLevel,
        owner: Entity?,
        state: ShotStateViewer,
        coordinate: AnchoredCoordinate,
        directInvoker: Entity?
    )

    companion object {
        val HOOK = HookModule.HookBuilder("invoke_redirection", ServerInvokeRedirectionHook::class).invoking()
    }
}