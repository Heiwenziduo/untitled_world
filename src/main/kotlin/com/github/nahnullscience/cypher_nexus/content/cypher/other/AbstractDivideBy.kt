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
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.InvokingParameterBundle
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
        shotState: ShotStateChunk,
        data: HelperDataBundle,
        paras: InvokingParameterBundle,
        relativeIndex: Int,
        isCopy: Boolean
    ) {
        CypherNexus.debugCypher { "[$this $relativeIndex] is invoked and modifies the state" }
        modifyShotState(helper, data, shotState)

        // TODO targetIndex should be handled specially to achieve consistence with Noita (especially with Greek letters)
        val targetIndex = helper.peekNextIndex(relativeIndex + 1)
        val target = helper.peekNext(relativeIndex + 1) ?: return // step++ avoid infinite loop

        val currentDepth = paras.divideByChainLength++
        paras.divideByChainLengthMax = max(paras.divideByChainLengthMax, currentDepth)

        val canGoDeeper =  paras.divideByChainLength <= chainPositionLimit

        // first copy with draw-disabled, others with draw-enabled // every Dx exceed its position limit will turn to "D1"
        paras.disableDraw()
        copyCypher(target, helper, shotState, data, paras, targetIndex)
        paras.enableDraw()

        if (canGoDeeper) {
            for (i in 0 until divideBy - 1) {
                copyCypher(target, helper, shotState, data, paras, targetIndex)
            }
        }

        // if this is the beginning of one chain, do discard based on chain length
        if (currentDepth == 0) {
            CypherNexus.debugCypher { "divide by chain finish, discard next ${paras.divideByChainLengthMax + 1}" }
            for (i in 0 .. paras.divideByChainLengthMax) helper.deckNext2discard()
            paras.divideByChainLengthMax = 0
        }
        paras.divideByChainLength = currentDepth
    }

    object D2 : AbstractDivideBy("divide_by_2", 2, 4) {
        override fun defaultAttributes(): CypherDataMap.Builder {
            return super.defaultAttributes()
                .manaDrain(40f)
                .delay(3)
                .shotStateAttr(CypherAttributes.DAMAGE, AttributeOperator.ADD, -2.0)
                .shotStateAttr(CypherAttributes.EFFECT_RADIUS, AttributeOperator.MULTIPLY_BASE, -0.2)
        }
    }

    object D3 : AbstractDivideBy("divide_by_3", 3, 3) {
        override fun defaultAttributes(): CypherDataMap.Builder {
            return super.defaultAttributes()
                .manaDrain(100f)
                .delay(5)
                .shotStateAttr(CypherAttributes.DAMAGE, AttributeOperator.ADD, -3.0)
                .shotStateAttr(CypherAttributes.EFFECT_RADIUS, AttributeOperator.MULTIPLY_BASE, -0.3)
        }
    }

    object D4 : AbstractDivideBy("divide_by_4", 4, 3) {
        override fun defaultAttributes(): CypherDataMap.Builder {
            return super.defaultAttributes()
                .manaDrain(180f)
                .delay(7)
                .shotStateAttr(CypherAttributes.DAMAGE, AttributeOperator.ADD, -4.0)
                .shotStateAttr(CypherAttributes.EFFECT_RADIUS, AttributeOperator.MULTIPLY_BASE, -0.4)
        }
    }

    object D10 : AbstractDivideBy("divide_by_10", 10, 2) {
        override fun defaultAttributes(): CypherDataMap.Builder {
            return super.defaultAttributes()
                .manaDrain(320f)
                .delay(15)
                .shotStateAttr(CypherAttributes.DAMAGE, AttributeOperator.ADD, -10.0)
                .shotStateAttr(CypherAttributes.EFFECT_RADIUS, AttributeOperator.MULTIPLY_BASE, -1.0)
        }
    }
}