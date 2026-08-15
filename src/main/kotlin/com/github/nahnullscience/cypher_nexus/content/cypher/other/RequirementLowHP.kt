package com.github.nahnullscience.cypher_nexus.content.cypher.other

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingSharedParameter
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk
import net.minecraft.world.entity.LivingEntity

object RequirementLowHP : AbstractRequirement.RequirementIf() {
    override fun requirement(
        helper: InvokingHelper,
        shotState: ShotStateChunk,
        data: InvokingHelper.HelperDataBundle,
        paras: InvokingSharedParameter,
    ): Boolean {
        if (helper.invoker !is LivingEntity) return false
        return helper.invoker.health / helper.invoker.maxHealth <= 0.25
    }

    override val resource = CypherNexus.modResource("requirement_low_hp")
}