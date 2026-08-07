package com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.invoking

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookModule
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.IHook
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk.ShotStateAccessor
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity

interface ServerInvokeAbortReleaseHook : IHook {

    /**
     * @return determine whether this release should be aborted
     * */
    fun abortReleaseServer(
        index: Int,
        count: Int,
        level: ServerLevel,
        owner: Entity?,
        state: ShotStateAccessor,
    ) : ReleaseAbort

    companion object {
        val HOOK = HookModule.HookBuilder("invoke_abort_release", ServerInvokeAbortReleaseHook::class).invoking()
    }

    enum class ReleaseAbort {
        CONTINUE,
        ABORT
    }
}