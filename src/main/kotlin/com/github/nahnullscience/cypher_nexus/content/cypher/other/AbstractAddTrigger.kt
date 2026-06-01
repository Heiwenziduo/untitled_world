package com.github.nahnullscience.cypher_nexus.content.cypher.other

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategories
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractNonProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataAttach
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.EmptyCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileNode
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileStateChunk
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.TriggerType

abstract class AbstractAddTrigger(
    private val _manaDrain: Float
) : AbstractNonProjectileCypher() {
    override val category = CypherCategories.OTHER
    abstract val triggerType: TriggerType
    override fun modifyStateChunk(helper: InvokingHelper, chunk: ProjectileStateChunk) = Unit
    override fun defaultAttributes() = super.defaultAttributes().manaDrain(_manaDrain).draw(0)

    override fun invokeInHand(
        helper: InvokingHelper,
        chunk: ProjectileStateChunk,
        data: InvokingHelper.HelperDataBundle,
        state: InvokingHelper.HelperStateBundle,
        options: CypherInvokingOptions
    ) {
        CypherNexus.LOGGER.debug("[{}] is invoked", this)
        val startIndex = data.deck.countTrailingZeroBits()
        var attachIndex = startIndex
        var cy: AbstractProjectileCypher = EmptyCypher
        while (attachIndex < helper.aoc.size) {
            // step 1, find target projectile cypher
            val cy0 = helper.aoc[attachIndex]
            attachIndex++
            if (!cy0.isInvokable()) continue

            cy0.modifyStateChunk(helper, chunk)

            if (cy0 is AbstractProjectileCypher && cy0.triggerCanAttach()) {
                cy = cy0
                break
            }
        }

        if (cy.isNotEmpty()) {
            // discard if attach is found
            CypherNexus.LOGGER.debug("[{}] find trigger attachable [{}]", this, cy)
            helper.deck2discard(startIndex, attachIndex)
            // TODO check cy#related-projectile to fit Noita mechanic (like blood magic)

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
                CypherNexus.LOGGER.debug("invoke [{}] with payload", cy)
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