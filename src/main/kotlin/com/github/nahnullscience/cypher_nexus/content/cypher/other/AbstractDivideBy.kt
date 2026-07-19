package com.github.nahnullscience.cypher_nexus.content.cypher.other

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategories
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractNonProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.IRecursiveCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.AttributeOperator
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.HelperDataBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.InvokingStateBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk
import kotlin.math.max

abstract class AbstractDivideBy(
    path: String,
    val divideBy: Int,
    val chainPositionLimit: Int
) : AbstractNonProjectileCypher(), IRecursiveCypher {

    final override val resource = CypherNexus.modResource(path)
    final override val category = CypherCategories.OTHER
    override val isRecursive = false
    override fun defaultAttributes() = super.defaultAttributes().draw(0)

    override fun invoke(
        helper: InvokingHelper,
        chunk: ShotStateChunk,
        data: HelperDataBundle,
        state: InvokingStateBundle,
        relativeIndex: Int,
        isCopy: Boolean
    ) {
        CypherNexus.debugCypher { "[$this $relativeIndex] is invoked and modifies the state" }
        modifyStateChunk(helper, data, chunk)

        // TODO targetIndex should be handled specially to achieve consistence with Noita (especially with Greek letters)
        val targetIndex = helper.peekNextIndex(relativeIndex + 1)
        val target = helper.peekNext(relativeIndex + 1) ?: return // step++ avoid infinite loop

        val currentDepth = state.divideByChainLength++
        state.divideByChainLengthMax = max(state.divideByChainLengthMax, currentDepth)

        val canGoDeeper =  state.divideByChainLength <= chainPositionLimit

        // first copy with draw-disabled, others with draw-enabled // every Dx exceed its position limit will turn to "D1"
        state.disableDraw()
        copyCypher(target, helper, chunk, data, state, targetIndex)
        state.enableDraw()

        if (canGoDeeper) {
            for (i in 0 until divideBy - 1) {
                copyCypher(target, helper, chunk, data, state, targetIndex)
            }
        }

        // if this is the beginning of one chain, do discard based on chain length
        if (currentDepth == 0) {
            CypherNexus.debugCypher { "divide by chain finish, discard next ${state.divideByChainLengthMax + 1}" }
            for (i in 0 .. state.divideByChainLengthMax) helper.deckNext2discard()
            state.divideByChainLengthMax = 0
        }
        state.divideByChainLength = currentDepth
    }

    object D2 : AbstractDivideBy("divide_by_2", 2, 4) {
        override fun defaultAttributes(): CypherDataMap.Builder {
            return super.defaultAttributes()
                .manaDrain(40f)
                .delay(3)
                .stateChunkAttr(CypherAttributes.DAMAGE, AttributeOperator.ADD, -2.0)
                .stateChunkAttr(CypherAttributes.EFFECT_RADIUS, AttributeOperator.MULTIPLY_BASE, -0.2)
        }
    }

    object D3 : AbstractDivideBy("divide_by_3", 3, 3) {
        override fun defaultAttributes(): CypherDataMap.Builder {
            return super.defaultAttributes()
                .manaDrain(100f)
                .delay(5)
                .stateChunkAttr(CypherAttributes.DAMAGE, AttributeOperator.ADD, -3.0)
                .stateChunkAttr(CypherAttributes.EFFECT_RADIUS, AttributeOperator.MULTIPLY_BASE, -0.3)
        }
    }

    object D4 : AbstractDivideBy("divide_by_4", 4, 3) {
        override fun defaultAttributes(): CypherDataMap.Builder {
            return super.defaultAttributes()
                .manaDrain(180f)
                .delay(7)
                .stateChunkAttr(CypherAttributes.DAMAGE, AttributeOperator.ADD, -4.0)
                .stateChunkAttr(CypherAttributes.EFFECT_RADIUS, AttributeOperator.MULTIPLY_BASE, -0.4)
        }
    }

    object D10 : AbstractDivideBy("divide_by_10", 10, 2) {
        override fun defaultAttributes(): CypherDataMap.Builder {
            return super.defaultAttributes()
                .manaDrain(320f)
                .delay(15)
                .stateChunkAttr(CypherAttributes.DAMAGE, AttributeOperator.ADD, -10.0)
                .stateChunkAttr(CypherAttributes.EFFECT_RADIUS, AttributeOperator.MULTIPLY_BASE, -1.0)
        }
    }
}