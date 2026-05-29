package com.github.nahnullscience.cypher_nexus.content.cypher.other

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileStateChunk

object RequirementHP : AbstractRequirement.RequirementIf() {
    override fun requirement(
        helper: InvokingHelper,
        chunk: ProjectileStateChunk,
        data: InvokingHelper.HelperDataBundle,
        state: InvokingHelper.HelperStateBundle,
        options: CypherInvokingOptions
    ): Boolean {
        helper.invoker?: return false
        return helper.invoker.health / helper.invoker.maxHealth <= 0.25
    }

    override val resource = CypherNexus.modResource("requirement_hp")
}