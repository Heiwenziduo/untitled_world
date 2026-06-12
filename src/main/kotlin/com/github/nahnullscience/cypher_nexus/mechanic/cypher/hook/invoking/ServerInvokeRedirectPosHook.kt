package com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.invoking

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractCypherProjectile
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookModule
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookModule.HookType.PROJECTILE
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
    fun redirectPosDireServer(
        level: ServerLevel,
        invoker: Entity?,
        owner: Entity?,
        projectile: AbstractCypherProjectile,
        strength: Int,
        pair: PosDirePair,
        index: Int
    ): PosDirePair

    companion object {
        val MODULE = HookModule(
            CypherNexus.modResource("invoke_redirect_pos"),
            ServerInvokeRedirectPosHook::class,
            false,
            PROJECTILE,
            false
        )
    }
}