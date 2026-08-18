package com.github.nahnullscience.cypher_nexus.content.cypher.utility

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategories
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractNonProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.invoking.ServerInvokeRedirectionHook
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotState.ShotStateViewer
import com.github.nahnullscience.cypher_nexus.utility.linear_space.AnchoredCoordinate
import com.github.nahnullscience.cypher_nexus.utility.linear_space.anchor
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity

class InnerForceCypher(
    defaultAttribute: CypherDataMap.Builder.() -> CypherDataMap.Builder
) : AbstractNonProjectileCypher(defaultAttribute), ServerInvokeRedirectionHook {
    override val resource = CypherNexus.modResource("inner_force")
    override val category = CypherCategories.UTILITY

    override fun invokeRedirectServer(
        index: Int,
        count: Int,
        level: ServerLevel,
        owner: Entity?,
        state: ShotStateViewer,
        coordinate: AnchoredCoordinate,
        directInvoker: Entity?
    ) {
        owner ?: return
        coordinate.anchor(owner.eyePosition)
    }
}