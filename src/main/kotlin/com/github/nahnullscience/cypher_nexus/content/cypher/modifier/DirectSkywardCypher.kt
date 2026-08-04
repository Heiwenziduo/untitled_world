package com.github.nahnullscience.cypher_nexus.content.cypher.modifier

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ModifierCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.invoking.ServerInvokePosRedirectionHook
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk.ShotStateViewer
import com.github.nahnullscience.cypher_nexus.utility.PosDirePair
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3

class DirectSkywardCypher(
    defaultAttribute: CypherDataMap.Builder.() -> CypherDataMap.Builder
) : ModifierCypher(defaultAttribute), ServerInvokePosRedirectionHook {
    override val resource = CypherNexus.modResource("direct_skyward")
    override fun redirectPosDireServer(
        index: Int,
        count: Int,
        level: ServerLevel,
        owner: Entity?,
        state: ShotStateViewer,
        directInvoker: Entity?,
        pair: PosDirePair
    ): PosDirePair {
        return if (pair.direction != Vec3.ZERO) PosDirePair(pair.position, Direction.UP.unitVec3) else pair
    }
}

class DirectGroundwardCypher(
    defaultAttribute: CypherDataMap.Builder.() -> CypherDataMap.Builder
) : ModifierCypher(defaultAttribute), ServerInvokePosRedirectionHook {
    override val resource = CypherNexus.modResource("direct_groundward")
    override fun redirectPosDireServer(
        index: Int,
        count: Int,
        level: ServerLevel,
        owner: Entity?,
        state: ShotStateViewer,
        directInvoker: Entity?,
        pair: PosDirePair
    ): PosDirePair {
        return if (pair.direction != Vec3.ZERO) PosDirePair(pair.position, Direction.DOWN.unitVec3) else pair
    }
}