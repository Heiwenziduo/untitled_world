package com.github.nahnullscience.cypher_nexus.content.cypher.other

import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategoryRegistry
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractNonProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.EmptyCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileNode
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileStateChunk
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.TriggerType

abstract class AbstractAddTrigger(
    override val manaDrain: Float
) : AbstractNonProjectileCypher() {
    override val draw: Int = 0
    override val category = CypherCategoryRegistry.OTHER
    abstract val triggerType: TriggerType

    override fun invokeInHand(
        helper: InvokingHelper,
        chunk: ProjectileStateChunk,
        data: InvokingHelper.HelperDataBundle,
        state: InvokingHelper.HelperStateBundle,
        options: CypherInvokingOptions
    ) {
        var attachIndex = data.deck.countTrailingZeroBits()
        var cy: AbstractProjectileCypher = EmptyCypher
        while (attachIndex < helper.aoc.size) {
            // step 1, find target projectile cypher
            val cy0 = helper.aoc[attachIndex]
            cy0.modifyStateChunk(helper, chunk)
            helper.deck2discard(attachIndex)
            attachIndex++
            if (cy0 is AbstractProjectileCypher && cy0.triggerCanAttach()) {
                cy = cy0
                break
            }
        }

        if (cy.isNotEmpty()) {
            // step 2, find payload
            var index1 = attachIndex
            var find = false
            while (index1 < helper.aoc.size) {
                val cy1 = helper.aoc[index1]
                index1++
                if (cy1 is AbstractProjectileCypher && cy1.triggerCanPayload()) {
                    find = true
                    break
                }
            }
            if (find) {
                val subChunk = ProjectileStateChunk(Int.MAX_VALUE)
                chunk.addProjectile(ProjectileNode(cy, subChunk, triggerType))
                val payload = helper.drawNext()
                payload?.invokeInHand(helper, subChunk, data, state, options)
            } else {
                chunk.addProjectile(ProjectileNode(cy, null))
            }
        }
    }
}