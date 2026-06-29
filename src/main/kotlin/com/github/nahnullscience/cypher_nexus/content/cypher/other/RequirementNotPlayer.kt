package com.github.nahnullscience.cypher_nexus.content.cypher.other

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk
import net.minecraft.world.entity.player.Player

object RequirementNotPlayer : AbstractRequirement.RequirementIf() {
    override fun requirement(
        helper: InvokingHelper,
        chunk: ShotStateChunk,
        data: InvokingHelper.HelperDataBundle,
        state: InvokingHelper.InvokingStateBundle,
    ): Boolean {
        return helper.invoker !is Player
    }

    override val resource = CypherNexus.modResource("requirement_not_player")
}