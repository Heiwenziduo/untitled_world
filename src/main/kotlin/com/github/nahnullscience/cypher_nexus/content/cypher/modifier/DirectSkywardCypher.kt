package com.github.nahnullscience.cypher_nexus.content.cypher.modifier

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ModifierCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.invoking.ServerInvokeRedirectionHook
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk.ShotStateViewer
import com.github.nahnullscience.cypher_nexus.utility.linear_space.AnchoredCoordinate
import com.github.nahnullscience.cypher_nexus.utility.linear_space.face
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity

class DirectSkywardCypher(
    defaultAttribute: CypherDataMap.Builder.() -> CypherDataMap.Builder
) : ModifierCypher(defaultAttribute), ServerInvokeRedirectionHook {
    override val resource = CypherNexus.modResource("direct_skyward")
    override fun invokeRedirectServer(
        index: Int,
        count: Int,
        level: ServerLevel,
        owner: Entity?,
        state: ShotStateViewer,
        coordinate: AnchoredCoordinate,
        directInvoker: Entity?
    ) {
        coordinate.face(Direction.UP.unitVec3)
    }
}

class DirectGroundwardCypher(
    defaultAttribute: CypherDataMap.Builder.() -> CypherDataMap.Builder
) : ModifierCypher(defaultAttribute), ServerInvokeRedirectionHook {
    override val resource = CypherNexus.modResource("direct_groundward")
    override fun invokeRedirectServer(
        index: Int,
        count: Int,
        level: ServerLevel,
        owner: Entity?,
        state: ShotStateViewer,
        coordinate: AnchoredCoordinate,
        directInvoker: Entity?
    ) {
        coordinate.face(Direction.DOWN.unitVec3)
    }
}