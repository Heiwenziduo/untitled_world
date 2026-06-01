package com.github.nahnullscience.cypher_nexus.content.cypher.other

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategories
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractNonProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileStateChunk

/** invoke the next a few cyphers only when the requirements are met */
sealed class AbstractRequirement : AbstractNonProjectileCypher() {
    override val category = CypherCategories.OTHER
    override fun modifyStateChunk(helper: InvokingHelper, chunk: ProjectileStateChunk) = Unit
    override fun defaultAttributes() = super.defaultAttributes().manaDrain(0f).draw(1)

    abstract class RequirementIf : AbstractRequirement() {
        /** true if conditions are met */
        abstract fun requirement(
            helper: InvokingHelper,
            chunk: ProjectileStateChunk,
            data: InvokingHelper.HelperDataBundle,
            state: InvokingHelper.HelperStateBundle,
            options: CypherInvokingOptions
        ) : Boolean

        override fun invokeInHand(
            helper: InvokingHelper,
            chunk: ProjectileStateChunk,
            data: InvokingHelper.HelperDataBundle,
            state: InvokingHelper.HelperStateBundle,
            options: CypherInvokingOptions
        ) {
            CypherNexus.LOGGER.debug("[{}] is invoked", this)
            val currentIndex = data.deck.countTrailingZeroBits()
            val ok = requirement(helper, chunk, data, state, options)
            var otherwise = -1
            var endpoint = -1
            for (i in currentIndex until helper.aoc.size) {
                val cy = helper.aoc[i]
                if (cy is RequirementOtherwise) otherwise = i
                if (cy is RequirementEndpoint) {
                    endpoint = i
                    break
                }
                if (cy is RequirementIf) break
            }

            CypherNexus.LOGGER.debug("[{}] requirement is {} met", this, if (ok) "" else "not")
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
                    helper.deck2discard(currentIndex, otherwise + 1)
                    // if there is an "else", discard everything between them
                    // in this specific case, endpoint search is unnecessary... well
                } else if (endpoint > 0) {
                    helper.deck2discard(currentIndex, endpoint + 1)
                } else {
                    helper.deck2discard(currentIndex)
                }
            }

            if (options.drawEnabled) handleDraws(helper, chunk, data, state, options)
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