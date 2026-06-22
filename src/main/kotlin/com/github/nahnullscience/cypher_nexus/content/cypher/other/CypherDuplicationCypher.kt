package com.github.nahnullscience.cypher_nexus.content.cypher.other

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategories
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractNonProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.HelperDataBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.InvokingStateBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileStateChunk

object CypherDuplicationCypher : AbstractNonProjectileCypher() {
    override val resource = CypherNexus.modResource("cypher_duplication")
    override val category = CypherCategories.OTHER
    override val isRecursive = true

    override fun defaultAttributes(): CypherDataMap.Builder {
        return super.defaultAttributes()
            .manaDrain(300f)
            .draw(1)
            .delay(7)
            .recharge(7)
    }

    override fun invoke(
        helper: InvokingHelper,
        chunk: ProjectileStateChunk,
        data: HelperDataBundle,
        state: InvokingStateBundle,
        relativeIndex: Int,
        isCopy: Boolean
    ) {
        if (!canRecursionContinue(state)) return

        CypherNexus.debugCypher { "[$this $relativeIndex] is invoked and modifies the state" }

        modifyStateChunk(helper, data, chunk)

        duplicate(helper, chunk, data, state, relativeIndex)

        handleDraws(helper, chunk, data, state)
    }

    // re-invoke every cypher in Hand
    private fun duplicate(
        helper: InvokingHelper,
        chunk: ProjectileStateChunk,
        data: HelperDataBundle,
        state: InvokingStateBundle,
        relativeIndex: Int,
    ) {
        val hand = data.hand
        for (i in hand.countTrailingZeroBits() until relativeIndex) {
            val cy = helper.aoc.getInvokableOrNull(i) ?: continue
            if (cy is CypherDuplicationCypher) continue

            CypherNexus.debugCypher { "[$this] copies $cy" }
            val depth = state.recursionDepth++
            cy.invoke(helper, chunk, data, state, i, true)
            state.recursionDepth = depth
        }
    }
}