package com.github.nahnullscience.cypher_nexus.content.cypher.other

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.content.cypher.utility.RefresherRingCypher
import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategories
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractNonProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap.Builder
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.IRecursiveCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ModifierCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.StaticProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.HelperDataBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingSharedParameter
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk

/**
 * a series of cypher that can invoke other cyphers
 * */
abstract class AbstractGreekLetter(
    path: String,
    defaultAttribute: Builder.() -> Builder,
) : AbstractNonProjectileCypher(defaultAttribute), IRecursiveCypher {
    override val resource = CypherNexus.modResource(path)
    override val category = CypherCategories.OTHER
    override val isRecursive = true

    override fun invoke(
        helper: InvokingHelper,
        shotState: ShotStateChunk,
        data: HelperDataBundle,
        paras: InvokingSharedParameter,
        relativeIndex: Int,
        isCopy: Boolean
    ) {
        CypherNexus.debugCypher { "[$this $relativeIndex] is invoked and modifies the state" }
        modifyShotState(helper, shotState, data, paras, isCopy)
    }

    /** the first */
    // reduce some mana cost since we don't have limited-charge cyphers
    class Alpha(defaultAttribute: Builder.() -> Builder) : AbstractGreekLetter("alpha", defaultAttribute) {
        override fun invoke(
            helper: InvokingHelper,
            shotState: ShotStateChunk,
            data: HelperDataBundle,
            paras: InvokingSharedParameter,
            relativeIndex: Int,
            isCopy: Boolean
        ) {
            super.invoke(helper, shotState, data, paras, relativeIndex, isCopy)
            // find first through: discard -> hand -> deck
            // since we don't keep a list but use bits instead, the order of certain card pile
            // may not be identical to the order they get there, especially for discard
            val target = sequenceOf(
                helper.data.discard,
                helper.data.hand,
                helper.data.deck,
            ).firstOrNull { it != 0L }

            if (target != null) {
                copyCypherIndexed(target.countTrailingZeroBits(), helper, shotState, data, paras, relativeIndex)
            } else
                CypherNexus.debugCypher { "[$this $relativeIndex] didn't find a valid target" }
        }
    }

    /** the last */
    class Gamma(defaultAttribute: Builder.() -> Builder) : AbstractGreekLetter("gamma", defaultAttribute) {
        override fun invoke(
            helper: InvokingHelper,
            shotState: ShotStateChunk,
            data: HelperDataBundle,
            paras: InvokingSharedParameter,
            relativeIndex: Int,
            isCopy: Boolean
        ) {
            super.invoke(helper, shotState, data, paras, relativeIndex, isCopy)
            // find last through: deck -> hand -> discard
            val target = sequenceOf(
                helper.data.deck,
                helper.data.hand,
                helper.data.discard,
            ).firstOrNull { it != 0L }

            if (target != null) {
                copyCypherIndexed((63 - target.countLeadingZeroBits()), helper, shotState, data, paras, relativeIndex)
            } else
                CypherNexus.debugCypher { "[$this $relativeIndex] didn't find a valid target" }
        }
    }

    /** every thing */
    class Omega(defaultAttribute: Builder.() -> Builder) : AbstractGreekLetter("omega", defaultAttribute) {
        override fun invoke(
            helper: InvokingHelper,
            shotState: ShotStateChunk,
            data: HelperDataBundle,
            paras: InvokingSharedParameter,
            relativeIndex: Int,
            isCopy: Boolean
        ) {
            super.invoke(helper, shotState, data, paras, relativeIndex, isCopy)
            helper.aoc.invokableForEach { index, cypher ->
                if (helper.isIndexInHand(index) && cypher is IRecursiveCypher && cypher.isRecursive) {
                    // CypherNexus.debugCypher { "" }
                    // recursive cyphers in hand will be skipped
                    return@invokableForEach
                }

                // RefresherRing will be skipped
                if (cypher is RefresherRingCypher) return@invokableForEach

                // disable draw for every cypher, in case copied cypher turns draw on
                paras.disableDraw()
                copyCypher(cypher, helper, shotState, data, paras, relativeIndex)
                paras.enableDraw()
            }
        }
    }

    /** next two */
    class Tau(defaultAttribute: Builder.() -> Builder) : AbstractGreekLetter("tau", defaultAttribute) {
        override fun invoke(
            helper: InvokingHelper,
            shotState: ShotStateChunk,
            data: HelperDataBundle,
            paras: InvokingSharedParameter,
            relativeIndex: Int,
            isCopy: Boolean
        ) {
            super.invoke(helper, shotState, data, paras, relativeIndex, isCopy)

            val merge = -1L shl (relativeIndex + 1)
            run {
                var count = 0
                helper.aoc.invokableForEach(merge) { index, cypher ->
                    count++
                    copyCypher(cypher, helper, shotState, data, paras, relativeIndex)
                    if (count >= 2) return@run
                }
            }
        }
    }

    /** copy all modifier */
    class Mu(defaultAttribute: Builder.() -> Builder) : AbstractGreekLetter("mu", defaultAttribute) {
        override fun invoke(
            helper: InvokingHelper,
            shotState: ShotStateChunk,
            data: HelperDataBundle,
            paras: InvokingSharedParameter,
            relativeIndex: Int,
            isCopy: Boolean
        ) {
            super.invoke(helper, shotState, data, paras, relativeIndex, isCopy)
            helper.aoc.invokableForEach { index, cypher ->
                if (cypher is ModifierCypher) {
                    paras.disableDraw()
                    copyCypher(cypher, helper, shotState, data, paras, relativeIndex)
                    paras.enableDraw()
                }
            }
            // draw one time unconditionally
            drawXForEach(helper, draw) { index, cypher ->
                cypher.invokeInHand(helper, shotState, data, paras)
            }
        }
    }

    /** all projectile */
    class Phi(defaultAttribute: Builder.() -> Builder) : AbstractGreekLetter("phi", defaultAttribute) {
        override fun invoke(
            helper: InvokingHelper,
            shotState: ShotStateChunk,
            data: HelperDataBundle,
            paras: InvokingSharedParameter,
            relativeIndex: Int,
            isCopy: Boolean
        ) {
            super.invoke(helper, shotState, data, paras, relativeIndex, isCopy)
            helper.aoc.invokableForEach { index, cypher ->
                if (cypher is ProjectileCypher<*>) {
                    paras.disableDraw()
                    copyCypher(cypher, helper, shotState, data, paras, relativeIndex)
                    paras.enableDraw()
                }
            }
        }
    }

    /** all static */
    class Sigma(defaultAttribute: Builder.() -> Builder) : AbstractGreekLetter("sigma", defaultAttribute) {
        override fun invoke(
            helper: InvokingHelper,
            shotState: ShotStateChunk,
            data: HelperDataBundle,
            paras: InvokingSharedParameter,
            relativeIndex: Int,
            isCopy: Boolean
        ) {
            super.invoke(helper, shotState, data, paras, relativeIndex, isCopy)
            helper.aoc.invokableForEach { index, cypher ->
                if (cypher is StaticProjectileCypher<*>) {
                    paras.disableDraw()
                    copyCypher(cypher, helper, shotState, data, paras, relativeIndex)
                    paras.enableDraw()
                }
            }
            // draw one time unconditionally
            drawXForEach(helper, draw) { index, cypher ->
                cypher.invokeInHand(helper, shotState, data, paras)
            }
        }
    }
}