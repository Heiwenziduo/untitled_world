package com.github.nahnullscience.cypher_nexus.content.cypher.other

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategories
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractNonProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttributeOperation
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.HelperDataBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.InvokingStateBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileStateChunk

abstract class AbstractDivideBy() : AbstractNonProjectileCypher() {
    override val category = CypherCategories.OTHER
    override val isRecursive = false
    override fun defaultAttributes() = super.defaultAttributes().draw(0)

    abstract val divideBy: Int
    abstract val chainPositionLimit: Int

    override fun invoke(
        helper: InvokingHelper,
        chunk: ProjectileStateChunk,
        data: HelperDataBundle,
        state: InvokingStateBundle,
        relativeIndex: Int
    ) {
        super.invoke(helper, chunk, data, state, relativeIndex)

        val targetIndex = helper.peekNextIndex(relativeIndex + 1)
        val target = helper.peekNext(relativeIndex + 1) ?: return // step++ avoid infinite loop

        CypherNexus.LOGGER.debug("[{}] will copy: [{}]", this, target)

        val currentDepth = state.divideByChainLength++

        val canGoDeeper =  state.divideByChainLength <= chainPositionLimit

        // first copy with draw-disabled, others with draw-enabled // every Dx exceed its position limit will turn to "D1"
        state.drawEnabled = false
        target.invoke(helper, chunk, data, state, targetIndex)
        state.drawEnabled = true
        if (canGoDeeper) {
            for (i in 0 until divideBy - 1) {
                target.invoke(helper, chunk, data, state, targetIndex)
            }
        }
        // if this is the beginning of one chain, do discard based on chain length
        if (currentDepth == 0) {
            CypherNexus.LOGGER.debug("divide by chain finish, discard next {}", state.divideByChainLength + 1)
            for (i in 0 .. state.divideByChainLength) helper.deckNext2discard()
        }
        state.divideByChainLength = currentDepth
    }

    object D2 : AbstractDivideBy() {
        override val resource = CypherNexus.modResource("divide_by_2")
        override val divideBy = 2
        override val chainPositionLimit = 4
        override fun defaultAttributes(): CypherDataMap.Builder {
            return super.defaultAttributes()
                .manaDrain(70f)
                .delay(3)
                .stateChunkAttr(CypherAttributes.DAMAGE, CypherAttributeOperation.ADD, -2.0)
                .stateChunkAttr(CypherAttributes.EFFECT_RADIUS, CypherAttributeOperation.MULTIPLY_BASE, -0.1)
        }
    }

    object D3 : AbstractDivideBy() {
        override val resource = CypherNexus.modResource("divide_by_3")
        override val divideBy = 3
        override val chainPositionLimit = 3
        override fun defaultAttributes(): CypherDataMap.Builder {
            return super.defaultAttributes()
                .manaDrain(110f)
                .delay(5)
                .stateChunkAttr(CypherAttributes.DAMAGE, CypherAttributeOperation.ADD, -3.0)
                .stateChunkAttr(CypherAttributes.EFFECT_RADIUS, CypherAttributeOperation.MULTIPLY_BASE, -0.15)
        }
    }

    object D4 : AbstractDivideBy() {
        override val resource = CypherNexus.modResource("divide_by_4")
        override val divideBy = 4
        override val chainPositionLimit = 3
        override fun defaultAttributes(): CypherDataMap.Builder {
            return super.defaultAttributes()
                .manaDrain(150f)
                .delay(7)
                .stateChunkAttr(CypherAttributes.DAMAGE, CypherAttributeOperation.ADD, -4.0)
                .stateChunkAttr(CypherAttributes.EFFECT_RADIUS, CypherAttributeOperation.MULTIPLY_BASE, -0.2)
        }
    }

    object D10 : AbstractDivideBy() {
        override val resource = CypherNexus.modResource("divide_by_10")
        override val divideBy = 10
        override val chainPositionLimit = 2
        override fun defaultAttributes(): CypherDataMap.Builder {
            return super.defaultAttributes()
                .manaDrain(240f)
                .delay(15)
                .stateChunkAttr(CypherAttributes.DAMAGE, CypherAttributeOperation.ADD, -10.0)
                .stateChunkAttr(CypherAttributes.EFFECT_RADIUS, CypherAttributeOperation.MULTIPLY_BASE, -0.5)
        }
    }
}