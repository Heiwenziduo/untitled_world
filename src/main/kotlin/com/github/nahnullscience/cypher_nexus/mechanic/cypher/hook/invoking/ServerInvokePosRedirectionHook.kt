package com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.invoking

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookModule
import com.github.nahnullscience.cypher_nexus.utility.mod.PosDirePair
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity

interface ServerInvokePosRedirectionHook {
    /**
     * @param level Level
     * @param directInvoker who invokes the cypher, this may be another cypher (trigger)
     * @param owner the entity that fires the cypher
     * @param strength how many times the cypher is invoked
     * @param pair cumulated position & direction, will be forward
     * @param index some mythic number
     * */
    fun redirectPosDireServer(
        level: ServerLevel,
        directInvoker: Entity?,
        owner: Entity?,
        strength: Int,
        pair: PosDirePair,
        index: Int
    ): PosDirePair

    companion object {
        val HOOK = HookModule.HookBuilder("invoke_pos_redirect", ServerInvokePosRedirectionHook::class).invoking()
    }
}