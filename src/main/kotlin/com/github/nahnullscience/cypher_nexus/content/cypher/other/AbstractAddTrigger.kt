package com.github.nahnullscience.cypher_nexus.content.cypher.other

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategories
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractNonProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher.Companion.TRIGGER_CHARGE_MAX
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.IRecursiveCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.HelperDataBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.InvokingParameterBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.TriggerType

abstract class AbstractAddTrigger(
    private val _manaDrain: Float
) : AbstractNonProjectileCypher(), IRecursiveCypher {
    override val category = CypherCategories.OTHER
    override val isRecursive = false
    abstract val addTrigger: TriggerType
    init {
        require(addTrigger != TriggerType.NONE)
    }

    override fun modifyShotState(
        helper: InvokingHelper,
        shotState: ShotStateChunk,
        data: HelperDataBundle,
        paras: InvokingParameterBundle,
        isCopy: Boolean
    ) = Unit
    override fun defaultAttributes() = super.defaultAttributes().manaDrain(_manaDrain).draw(0)

    override fun invoke(
        helper: InvokingHelper,
        shotState: ShotStateChunk,
        data: HelperDataBundle,
        paras: InvokingParameterBundle,
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
                else if (cypher is AbstractNonProjectileCypher) {
                    // only absorb attributes and ignore other logic
                    // projectile-related cyphers with triggerInterplay == false will also be ignored, e.g. the Notes
                    cypher.modifyShotState(helper, shotState, data, paras, true)
                    CypherNexus.debugCypher { "[$this] modify the state through [$cypher $index]" }
                }
            }
        }
        if (cy1 == null || !cy1.isInvokable) return
        if (cy1 !is AbstractProjectileCypher<*>) {
            // to fit Noita mechanic, let's agree a Non-Proj cypher with #triggerInterplay == true will terminate trigger-attachment
            // for example, refresher-ring // in addition, attachable projectile not found also skip the discard process
            // leave the deck unchanged
            CypherNexus.debugCypher { "[$this] attach process terminate due to [$cy1 $attachIndex]" }
            return
        }

        cy1.modifyShotState(helper, shotState, data, paras, true)
        CypherNexus.debugCypher { "[$this] find trigger attachable [$cy1 $attachIndex]" }

        // discard only if attach is valid
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

        // the cypher who activates the payload process doesn't have to be the payload
        if (cy2 != null) {
            CypherNexus.debugCypher { "invoke [$cy1] with payload due to [$cy2]" }
            for (i in 0 until cy1.projectileCount) {
                val subShot = cy1.addProjectileWithTrigger(shotState, addTrigger, TRIGGER_CHARGE_MAX)
                val payload = helper.drawNext()
                payload?.invokeInHand(helper, subShot, data, paras) ?: break
            }
        } else {
            cy1.addProjectileAlone(shotState)
        }
    }
}