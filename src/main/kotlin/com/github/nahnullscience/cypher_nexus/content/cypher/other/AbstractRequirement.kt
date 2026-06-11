package com.github.nahnullscience.cypher_nexus.content.cypher.other

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategories
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractNonProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.HelperDataBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.InvokingStateBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileStateChunk

/** invoke the next a few cyphers only when the requirements are met */
sealed class AbstractRequirement : AbstractNonProjectileCypher() {
    override val category = CypherCategories.OTHER
    override fun modifyStateChunk(
        helper: InvokingHelper,
        data: InvokingHelper.HelperDataBundle,
        chunk: ProjectileStateChunk
    ) = Unit
    override fun defaultAttributes() = super.defaultAttributes().manaDrain(0f).draw(1)

    abstract class RequirementIf : AbstractRequirement() {
        /** true if conditions are met */
        abstract fun requirement(
            helper: InvokingHelper,
            chunk: ProjectileStateChunk,
            data: InvokingHelper.HelperDataBundle,
            state: InvokingHelper.InvokingStateBundle,
        ) : Boolean

        override fun invoke(
            helper: InvokingHelper,
            chunk: ProjectileStateChunk,
            data: HelperDataBundle,
            state: InvokingStateBundle,
            relativeIndex: Int
        ) {
            CypherNexus.debugCypher { "[$this] is invoked and modifies the state" }
            val startIndex = helper.peekNextIndex(relativeIndex + 1)
            if (startIndex == -1) return handleDraws(helper, chunk, data, state)

            val ok = requirement(helper, chunk, data, state)
            var otherwise = -1
            var endpoint = -1
            for (i in startIndex until helper.aoc.invokableSize) {
                val cy = helper.aoc[i]
                if (cy is RequirementOtherwise) otherwise = i
                if (cy is RequirementEndpoint) {
                    endpoint = i
                    break
                }
                if (cy is RequirementIf) break
            }

            CypherNexus.debugCypher { "[$this] requirement is ${if (ok) "" else "not "}met" }
            if (ok) {
                if (otherwise > 0) {
                    if (endpoint > 0) helper.deck2discard(otherwise, endpoint + 1)
                    else {
                        helper.deck2discard(otherwise)
                        helper.deckNext2discard(otherwise) // discard otherwise itself and the next ONE if any
                    }
                    // this is different from Noita, which will discard directly till the end if no "close-parenthesis" after "else".
                }
            } else {
                if (otherwise > 0) {
                    helper.deck2discard(startIndex, otherwise + 1)
                    // if there is an "else", discard everything between them
                    // in this specific case, endpoint search is unnecessary... well
                } else if (endpoint > 0) {
                    helper.deck2discard(startIndex, endpoint + 1)
                } else {
                    helper.deckNext2discard(relativeIndex)
                }
            }

            handleDraws(helper, chunk, data, state)
        }
    }

    object RequirementOtherwise : AbstractRequirement() {
        // do nothing on its self
        override val resource = CypherNexus.modResource("requirement_otherwise")
    }

    object RequirementEndpoint : AbstractRequirement() {
        // do nothing on its self
        override val resource = CypherNexus.modResource("requirement_endpoint")
    }
}