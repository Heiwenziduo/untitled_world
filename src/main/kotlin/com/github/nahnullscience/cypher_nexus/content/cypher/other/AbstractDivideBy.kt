package com.github.nahnullscience.cypher_nexus.content.cypher.other

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategories
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractNonProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap.Builder
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.IRecursiveCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.HelperDataBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingSharedParameter
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk
import kotlin.math.max

abstract class AbstractDivideBy(
    defaultAttribute: Builder.() -> Builder,
    path: String,
    val divideByX: Int,
    val chainPosLimit: Int
) : AbstractNonProjectileCypher(defaultAttribute), IRecursiveCypher {

    final override val resource = CypherNexus.modResource(path)
    final override val category = CypherCategories.OTHER
    override val isRecursive = false

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

        // interplay with GreekLetters is not exactly identical with Noita
        // in Noita a GreekLetter can't be used in the middle of a DivideBy chain
        val targetIndex = helper.peekNextIndex(relativeIndex + 1)
        val target = helper.peekNext(relativeIndex + 1) ?: return // step++ avoid infinite loop

        val currentDepth = paras.diByChainDepthCurrent++
        paras.diByChainDepthMax = max(paras.diByChainDepthMax, currentDepth)

        val canGoDeeper =  currentDepth < chainPosLimit

        // first copy with draw-disabled, others with draw-enabled // every Dx exceed its position limit will turn to "D1"
        paras.disableDraw()
        copyCypher(target, helper, shotState, data, paras, targetIndex)
        paras.enableDraw()

        if (canGoDeeper) repeat(divideByX - 1) {
            copyCypher(target, helper, shotState, data, paras, targetIndex)
        }

        // if this is the beginning of one chain, do discard based on chain length
        if (currentDepth == 0) {
            CypherNexus.debugCypher { "divide by chain finish, discard next ${paras.diByChainDepthMax + 1}" }
            for (i in 0 .. paras.diByChainDepthMax) helper.deckNext2discard()
            paras.diByChainDepthMax = 0
        }
        paras.diByChainDepthCurrent = currentDepth
    }

    class D2(
        defaultAttribute: Builder.() -> Builder,
    ) : AbstractDivideBy(defaultAttribute, "divide_by_2", 2, 4)

    class D3(
        defaultAttribute: Builder.() -> Builder,
    ) : AbstractDivideBy(defaultAttribute, "divide_by_3", 3, 3)

    class D4(
        defaultAttribute: Builder.() -> Builder,
    ) : AbstractDivideBy(defaultAttribute, "divide_by_4", 4, 3)

    class D10(
        defaultAttribute: Builder.() -> Builder,
    ) : AbstractDivideBy(defaultAttribute, "divide_by_10", 10, 2)
}