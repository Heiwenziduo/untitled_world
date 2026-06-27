package com.github.nahnullscience.cypher_nexus.content.cypher.other

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.content.cypher.utility.RefresherRingCypher
import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategories
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractNonProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.IRecursiveCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.HelperDataBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.InvokingStateBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileStateChunk
import org.apache.logging.log4j.Level

/**
 * a series of cypher that can invoke other cyphers
 * */
abstract class AbstractGreekLetter(
    path: String,
    private val mana: Float
) : AbstractNonProjectileCypher(), IRecursiveCypher {
    override val resource = CypherNexus.modResource(path)
    override val category = CypherCategories.OTHER
    override val isRecursive = true
    override fun defaultAttributes() = super.defaultAttributes().manaDrain(mana)

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
    }

    /** the first */
    // reduce some mana cost since we don't have limited-charge cyphers
    object Alpha : AbstractGreekLetter("alpha", 20f) {
        override fun defaultAttributes() = super.defaultAttributes().delay(5)
        override fun invoke(
            helper: InvokingHelper,
            chunk: ProjectileStateChunk,
            data: HelperDataBundle,
            state: InvokingStateBundle,
            relativeIndex: Int,
            isCopy: Boolean
        ) {
            super.invoke(helper, chunk, data, state, relativeIndex, isCopy)
            // find first through: discard -> hand -> deck
            // since we don't keep a list but use bits instead, the order of certain card pile
            // may not be identical to the order they get there, especially for discard
            val target = sequenceOf(
                helper.data.discard,
                helper.data.hand,
                helper.data.deck,
            ).firstOrNull { it != 0L }

            if (target != null) {
                target.countTrailingZeroBits().let { index ->
                    val cy = helper.aoc.getInvokableOrNull(index)
                    if (cy == null) {
                        CypherNexus.debugCypher(Level.ERROR)
                        { "get uninvokable cypher on [index $index], this should never happen!" }
                        return@let
                    }

                    copyCypher(cy, helper, chunk, data, state, relativeIndex)
                }
            } else
                CypherNexus.debugCypher { "[$this $relativeIndex] didn't find a valid target" }
        }
    }

    /** the last */
    object Gamma : AbstractGreekLetter("gamma", 20f) {
        override fun defaultAttributes() = super.defaultAttributes().delay(5)
        override fun invoke(
            helper: InvokingHelper,
            chunk: ProjectileStateChunk,
            data: HelperDataBundle,
            state: InvokingStateBundle,
            relativeIndex: Int,
            isCopy: Boolean
        ) {
            super.invoke(helper, chunk, data, state, relativeIndex, isCopy)
            // find first through: deck -> hand -> discard
            val target = sequenceOf(
                helper.data.deck,
                helper.data.hand,
                helper.data.discard,
            ).firstOrNull { it != 0L }

            if (target != null) {
                (63 - target.countLeadingZeroBits()).let { index ->
                    val cy = helper.aoc.getInvokableOrNull(index)
                    if (cy == null) {
                        CypherNexus.debugCypher(Level.ERROR)
                        { "get uninvokable cypher on [index $index], this should never happen!" }
                        return@let
                    }

                    copyCypher(cy, helper, chunk, data, state, relativeIndex)
                }
            } else
                CypherNexus.debugCypher { "[$this $relativeIndex] didn't find a valid target" }
        }
    }

    /** every thing */
    object Omega : AbstractGreekLetter("omega", 300f) {
        override fun defaultAttributes() = super.defaultAttributes().delay(15)
        override fun invoke(
            helper: InvokingHelper,
            chunk: ProjectileStateChunk,
            data: HelperDataBundle,
            state: InvokingStateBundle,
            relativeIndex: Int,
            isCopy: Boolean
        ) {
            super.invoke(helper, chunk, data, state, relativeIndex, isCopy)
            helper.aoc.invokableForEach() { index, cypher ->
                if (helper.isIndexInHand(index) && cypher is IRecursiveCypher && cypher.isRecursive) {
                    // CypherNexus.debugCypher { "" }
                    // recursive cyphers in hand will be skipped
                    return@invokableForEach
                }

                // RefresherRing will be skipped
                if (cypher is RefresherRingCypher) return@invokableForEach

                state.disableDraw()
                copyCypher(cypher, helper, chunk, data, state, relativeIndex)
                state.enableDraw()
            }
        }
    }

    /** next two */
    object Tau : AbstractGreekLetter("tau", 80f) {
        override fun defaultAttributes() = super.defaultAttributes().delay(10)
        override fun invoke(
            helper: InvokingHelper,
            chunk: ProjectileStateChunk,
            data: HelperDataBundle,
            state: InvokingStateBundle,
            relativeIndex: Int,
            isCopy: Boolean
        ) {
            super.invoke(helper, chunk, data, state, relativeIndex, isCopy)

            val merge = -1L shl (relativeIndex + 1)
            run {
                var count = 0
                helper.aoc.invokableForEach(merge) { index, cypher ->
                    count++
                    copyCypher(cypher, helper, chunk, data, state, relativeIndex)
                    if (count >= 2) return@run
                }
            }
        }
    }
}