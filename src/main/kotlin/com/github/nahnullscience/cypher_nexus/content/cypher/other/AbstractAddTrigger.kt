package com.github.nahnullscience.cypher_nexus.content.cypher.other

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategories
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractNonProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.EmptyCypher
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
        recursionDepth: Int,
        isCopy: Boolean
    ) {
        CypherNexus.debugCypher { "[$this] is invoked" }
        val startIndex = helper.peekNextIndex(relativeIndex + 1)
        if (startIndex == -1) return // this means AddTrigger is the last one in deck

        var attachIndex = startIndex
        var cy: AbstractCypher = EmptyCypher
        while (attachIndex < helper.aoc.invokableSize) {
            // step 1, find target projectile cypher
            val cy0 = helper.aoc[attachIndex]
            attachIndex++
            if (!cy0.isInvokable()) continue

            if (cy0.triggerInterplay()) {
                cy = cy0
                break
            }

            // interplay-able && non-projectile -> do not modify state
            cy0.modifyStateChunk(helper, data, chunk)
            CypherNexus.debugCypher { "[$this] modify the state through [$cy0]" }
        }

        if (cy.isNotEmpty()) {
            if (cy !is AbstractProjectileCypher) {
                // to fit Noita mechanic, let's agree a NonProj cypher with #triggerCanAttach == true will terminate add trigger-s
                // for example, refresher-ring
                CypherNexus.debugCypher { "[$this] attach process terminate due to [$cy]" }
                return
            }
            cy.modifyStateChunk(helper, data, chunk)

            // discard if attach is found
            CypherNexus.debugCypher { "[$this] find trigger attachable [$cy]" }
            helper.deck2discard(startIndex, attachIndex)

            // step 2, find payload
            var index1 = attachIndex
            var find = false
            while (index1 < helper.aoc.invokableSize) {
                val cy1 = helper.aoc[index1]
                index1++
                if (cy1 is AbstractProjectileCypher && cy1.triggerInterplay()) {
                    find = true
                    break
                }
            }
            if (find) {
                CypherNexus.debugCypher { "invoke [$cy] with payload" }
                val subChunk = ProjectileStateChunk(Int.MAX_VALUE)
                chunk.addProjectile(ProjectileNode(cy, subChunk, triggerType))
                val payload = helper.drawNext()
                payload?.invokeInHand(helper, subChunk, data, state)
            } else {
                chunk.addProjectile(ProjectileNode(cy, null))
            }
        }
    }
}