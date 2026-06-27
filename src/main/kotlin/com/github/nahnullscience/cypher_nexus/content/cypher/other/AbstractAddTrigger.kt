package com.github.nahnullscience.cypher_nexus.content.cypher.other

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategories
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractNonProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.HelperDataBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.InvokingStateBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileNode
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileStateChunk
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.TriggerType

abstract class AbstractAddTrigger(
    private val _manaDrain: Float
) : AbstractNonProjectileCypher() {
    override val category = CypherCategories.OTHER
    abstract val triggerType: TriggerType
    override fun modifyStateChunk(
        helper: InvokingHelper,
        data: InvokingHelper.HelperDataBundle,
        chunk: ProjectileStateChunk
    ) = Unit
    override fun defaultAttributes() = super.defaultAttributes().manaDrain(_manaDrain).draw(0)

    override fun invoke(
        helper: InvokingHelper,
        chunk: ProjectileStateChunk,
        data: HelperDataBundle,
        state: InvokingStateBundle,
        relativeIndex: Int,
        isCopy: Boolean
    ) {
        CypherNexus.debugCypher { "[$this $relativeIndex] is invoked" }
        val startIndex = helper.peekNextIndex(relativeIndex + 1)
        if (startIndex == -1) return // this means AddTrigger is the last one in deck

        // step 1, find attachment
        var attachIndex = startIndex
        var cy1: AbstractCypher? = null
        run {
            helper.deckEach(startIndex) { index, cypher ->
                if (cypher.triggerInterplay()) {
                    attachIndex = index
                    cy1 = cypher
                    // return@deckEach just terminate current lambda, act as a "continue"
                    // so wrapper this with a run block and return there
                    return@run
                }
                cypher.modifyStateChunk(helper, data, chunk)
                CypherNexus.debugCypher { "[$this] modify the state through [$cypher $index]" }
            }
        }

        if (cy1 != null && cy1.isInvokable) {
            if (cy1 !is AbstractProjectileCypher) {
                // to fit Noita mechanic, let's agree a NonProj cypher with #triggerCanAttach == true will terminate add trigger-s
                // for example, refresher-ring
                CypherNexus.debugCypher { "[$this] attach process terminate due to [$cy1 $attachIndex]" }
                return
            }
            cy1.modifyStateChunk(helper, data, chunk)

            // discard if attach is found
            CypherNexus.debugCypher { "[$this] find trigger attachable [$cy1 $attachIndex]" }
            helper.deck2discard(startIndex, attachIndex + 1)

            // step 2, find payload
            var cy2: AbstractCypher? = null
            run {
                helper.deckEach(attachIndex + 1) { index, cypher ->
                    if (cypher.triggerInterplay()) {
                        cy2 = cypher
                        return@run
                    }
                }
            }

            // the cypher activates the payload process doesn't have to be the payload
            if (cy2 != null) {
                CypherNexus.debugCypher { "invoke [$cy1] with payload due to [$cy2]" }
                val subChunk = ProjectileStateChunk(Int.MAX_VALUE)
                chunk.addProjectile(ProjectileNode(cy1, subChunk, triggerType))
                val payload = helper.drawNext()
                payload?.invokeInHand(helper, subChunk, data, state)
            } else {
                chunk.addProjectile(ProjectileNode(cy1, null))
            }
        }
    }
}