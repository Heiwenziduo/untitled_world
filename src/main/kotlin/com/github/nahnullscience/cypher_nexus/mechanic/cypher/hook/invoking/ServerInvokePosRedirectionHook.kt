package com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.invoking

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookModule
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.IHook
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk.ShotStateViewer
import com.github.nahnullscience.cypher_nexus.utility.linear_space.PosDirePair
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity

interface ServerInvokePosRedirectionHook : IHook {

    fun redirectPosDireServer(
        index: Int,
        count: Int,
        level: ServerLevel,
        owner: Entity?,
        state: ShotStateViewer,
        directInvoker: Entity?,
        pair: PosDirePair,
    ): PosDirePair

    companion object {
        val HOOK = HookModule.HookBuilder("invoke_pos_redirect", ServerInvokePosRedirectionHook::class).invoking()
    }
}