package com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.invoking

import com.github.nahnullscience.cypher_nexus.content.entity.AbstractCypherProjectile
import com.github.nahnullscience.cypher_nexus.utility.mod.PosDirePair
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity

interface HookInvokeRedirectPosServer {
    /**
     * @param level Level
     * @param invoker who invokes the cypher
     * @param strength how many times the cypher is invoked
     * @param pair cumulated position & direction, will be forward
     * @param index some mythic number
     * */
    fun redirectPosDireServer(
        level: ServerLevel,
        invoker: Entity?,
        projectile: AbstractCypherProjectile,
        strength: Int,
        pair: PosDirePair,
        index: Int
    ): PosDirePair
}