package com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.WandDataFrequent
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.WandDataInvariable
import com.github.nahnullscience.cypher_nexus.utility.mod.ArrayOfCyphers
import com.github.nahnullscience.cypher_nexus.utility.mod.PosDirePair
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

/** cypher chain compiler */
class InvokingHelper (
    val level: Level,
    val invoker: LivingEntity?,
    val stack: ItemStack?,

    val wandStats: WandDataInvariable,
    val aoc: ArrayOfCyphers,
    val data: HelperDataBundle,
    /** direction doesn't have to be normalized */
    val invokePosDire: PosDirePair,
) {
    val rootChunk = ProjectileStateChunk()
    val states = HelperStateBundle()

    init {
        // define the order of bits go from right to left, which is inverse compare to the order of the cypherArray
        // number 5L (0b...0101) represents the first and the third cyphers
        if (data.deck == 0L) {
            data.deck = aoc.bits()
            data.discard = 0
        }
    }

    fun start() {
        while (data.draw >= 1) {
            val canContinue = step()
            if (!canContinue) break
        }
        rootChunk.release(level, invoker, invokePosDire)
        hand2discard()

        if (states.wrapped) {
            // force a reload
            data.index = 0
            data.deck = 0
            data.discard = aoc.bits()
        }
    }

    fun step(): Boolean {
        val cy = drawNext()
        if (cy != null) {
            cy.invokeInHand(this, rootChunk, data, states)
            data.draw --
            return true
        }
        return false
    }

    /** non-empty-cypher, null if deck is empty */
    fun drawNext(): AbstractCypher? {
        // find the index of the first '1' (last '0', in fact) starting from the right.
        val drawIndex = data.deck.countTrailingZeroBits()
        if (drawIndex >= aoc.size) return null
        return draw(drawIndex)
    }

    /** non-empty */
    private fun draw(index: Int): AbstractCypher? {
        val cy = aoc[index]
        CypherNexus.LOGGER.debug("draw [{}], the {}th cypher", cy, index + 1)
        if (cy.isEmpty()) {
            // this should not happen
            CypherNexus.LOGGER.error("draw [empty]: $index in $aoc, this should not happen")
            return null
        }
        if (data.manaCurrent < cy.manaDrain) {
            CypherNexus.LOGGER.debug("mana not enough, [{}] discards directly", cy)
            deck2discard(index)
            return drawNext()
        }
        deck2hand(index)
        data.manaCurrent -= cy.manaDrain
        return cy
    }

    // ============== bit operations ===================================================

    fun deck2hand(index: Int): AbstractCypher? {
        val cy = aoc[index]
        data.deck = data.deck and (1L shl index).inv()
        data.hand = data.hand or (1L shl index)
        return cy
    }
    fun hand2discard() {
        data.discard = data.discard or data.hand
        data.hand = 0
    }
    /** discard an exact index, do nothing if target index does not exist in deck */
    fun deck2discard(index: Int) {
        if (data.deck and (1L shl index) == 0L) return // target index is unavailable
        data.deck = data.deck and (1L shl index).inv()
        data.discard = data.discard or (1L shl index)

        val cy = aoc[index]
        CypherNexus.LOGGER.debug("[{}] discard from deck", cy)
    }
    /** discard next invokable */
    fun deckNext2discard(start: Int = 0) {
        val next = (data.deck shr start).countTrailingZeroBits() + start
        deck2discard(next)
    }
    /** start <= ... < end */
    fun deck2discard(from: Int, until: Int) {
        val filter = ((1L shl from) - 1).inv()
        val filter1 = ((1L shl until) - 1) and filter
        val toDiscard = data.deck and filter1

//        CypherNexus.LOGGER.debug("deck before bunch-discard: {}", data.deck.toString(2).padStart(20, '0'))

        data.deck = data.deck and toDiscard.inv()
        data.discard = data.discard or toDiscard

//        CypherNexus.LOGGER.debug("deck after bunch-discard: {}", data.deck.toString(2).padStart(20, '0'))

        for (i in toDiscard.countTrailingZeroBits() until 64 - toDiscard.countLeadingZeroBits()) {
            val cy = aoc[i]
            if (cy.isInvokable()) CypherNexus.LOGGER.debug("[{}] bunch-discard from deck", cy)
        }
    }
    /** aka. wrap, return true if there is something to wrap */
    fun discard2deck(): Boolean {
        CypherNexus.LOGGER.debug("discard {} wrap back into deck", data.discard.toString(2))
        val old = data.discard
        data.deck = data.deck or data.discard
        data.discard = 0
        return old > 0
    }
    // =================================================================================


    data class HelperDataBundle (
        var draw: Int,
        var index: Int,
        var delay: Int,
        var recharge: Int,
        var manaCurrent: Float,

        var hand: Long = 0,
        /** R -> L, invokable only */
        var deck: Long = 0,
        var discard: Long = 0,
    ) {
        fun frequentData() = WandDataFrequent(manaCurrent, index, delay, recharge, deck, discard)
        companion object {
            fun of(draw: Int, frequent: WandDataFrequent): HelperDataBundle {
                return HelperDataBundle(draw, frequent.index, frequent.delay, frequent.recharge, frequent.manaCurrent, 0, frequent.deck, frequent.discard)
            }
            fun of(invariable: WandDataInvariable, frequent: WandDataFrequent): HelperDataBundle {
                return HelperDataBundle(invariable.chunkI.draw, frequent.index, invariable.chunkI.castDelay, frequent.recharge + invariable.chunkI.rechargeTime, frequent.manaCurrent, 0, frequent.deck, frequent.discard)
            }
        }
    }

    data class HelperStateBundle (
        var wrapped: Boolean = false,
        var alreadyRefreshed: Boolean = false,
    )
}