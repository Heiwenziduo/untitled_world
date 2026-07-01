package com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.invoking

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation.ICypherBeforeInit
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookModule
import com.github.nahnullscience.cypher_nexus.utility.mod.PosDirePair
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity

interface ServerInvokeRedirectPosHook {
    /**
     * @param level Level
     * @param invoker who invokes the cypher, this may be another cypher (trigger)
     * @param owner the entity that fires the cypher
     * @param strength how many times the cypher is invoked
     * @param pair cumulated position & direction, will be forward
     * @param index some mythic number
     * */
    fun <CypherBeforeInit> redirectPosDireServer(
        level: ServerLevel,
        invoker: Entity?,
        owner: Entity?,
        cypherEntity: CypherBeforeInit,
        strength: Int,
        pair: PosDirePair,
        index: Int
    ): PosDirePair where CypherBeforeInit : Entity, CypherBeforeInit : ICypherBeforeInit

    companion object {
        val HOOK = HookModule.HookBuilder("invoke_redirect_pos", ServerInvokeRedirectPosHook::class).invoking()
    }
}