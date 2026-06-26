package com.github.nahnullscience.cypher_nexus.content.cypher.utility

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategories
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractNonProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.IRecursiveCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.HelperDataBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.InvokingStateBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileStateChunk

object ProteusCypher : AbstractNonProjectileCypher(), IRecursiveCypher {
    override val resource = CypherNexus.modResource("proteus")
    override val category = CypherCategories.UTILITY
    override val isRecursive = false
    override fun defaultAttributes(): CypherDataMap.Builder {
        return super.defaultAttributes()
            .manaDrain(10f)
            .draw(1)
    }

    override fun triggerInterplay() = true

    override fun invoke(
        helper: InvokingHelper,
        chunk: ProjectileStateChunk,
        data: HelperDataBundle,
        state: InvokingStateBundle,
        relativeIndex: Int,
        isCopy: Boolean
    ) {
        CypherNexus.debugCypher { "[$this $relativeIndex] is invoked and modifies the state" }
        modifyStateChunk(helper, data, chunk)

        if (state.drawEnabled)
        for (i in 0 until draw) {
            var cy = helper.drawNext()
            if (cy == null) {
                CypherNexus.debugCypher { "[$this] want a wrap" }
                val wrap = helper.wrap()
                if (!wrap) break
                cy = helper.drawNext()
            }
            if (cy != null) {
                val index = helper.relativeIndex
                cy.invokeInHand(helper, chunk, data, state)
                if (cy is ProteusCypher) data.manaCurrent += 100f // award some mana if finds self
                else {
                    copyCypher(cy, helper, chunk, data, state, index)
                }
            }
        }
    }
}