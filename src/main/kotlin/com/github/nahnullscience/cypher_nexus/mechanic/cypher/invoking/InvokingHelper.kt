package com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.ModDataAttachments.WAND_DATA_MAP
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.WandDataInvariable
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.WandInstance
import com.github.nahnullscience.cypher_nexus.utility.mod.ArrayOfCyphers
import com.github.nahnullscience.cypher_nexus.utility.mod.PosDirePair
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import kotlin.math.max

/** cypher chain compiler */
class InvokingHelper (
    val level: Level,
    val invoker: Entity?,

    val wandStats: WandDataInvariable,
    val aoc: ArrayOfCyphers,
    val data: HelperDataBundle,
    /** direction doesn't have to be normalized */
    val invokePosDire: PosDirePair,
) {
    val rootChunk = ProjectileStateChunk.root(this)
    val states = InvokingStateBundle()
    val isClientSide = level.isClientSide
    /** the position of last-drawn cypher */
    val relativeIndex: Int get() = 63 - data.hand.countLeadingZeroBits()

    init {
        // define the order of bits go from right to left, which is inverse compare to the order of the cypherArray
        // number 5L (0b...0101) represents the first and the third cyphers
        if (data.deck == 0L) {
            init()
        }
    }

    fun wandInstance(): WandInstance? {
        val has = invoker?.hasData(WAND_DATA_MAP)
        if (has ?: return null)
            return invoker
            .getData(WAND_DATA_MAP)
            .getOrPutInstance(wandStats, aoc, null, level)
        return null
    }

    // =================================================================================
    fun start() {
        // level.profiler.push("invoking-start") // F3 + L to record time cost
        CypherNexus.debugCypher { "invoking start, prepare cyphers" }

        while (data.draw >= 1) {
            val canContinue = step()
            if (!canContinue) break
        }
        hand2discard()

        if (states.wrapped) {
            // force a reload
            CypherNexus.debugCypher { "wand reload due to wrapped" }
            reload()
        }
        CypherNexus.debugCypher { "invoking finish: $data" }
        // level.profiler.pop()
    }
    fun finalizeInvoking() {
        rootChunk.release(level, invoker, invoker, invokePosDire)
        wandInstance()?.invokeFinish(level)
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

    // =================================================================================
//    /** peek the next cypher in the deck, null if empty
//     *  @param next the next x-th cypher */
//    fun peekNext(next: Int = 0): AbstractCypher? {
//        var peekIndex = data.deck.countTrailingZeroBits()
//        var stepIndex = peekIndex
//        var tmpDeck = data.deck shr (stepIndex + 1)
//
//        for (i in 0 until next) {
//            stepIndex = tmpDeck.countTrailingZeroBits()
//            peekIndex += ++stepIndex // step
//            tmpDeck = tmpDeck shr stepIndex
//            if (peekIndex >= aoc.invokableSize) return null
//        }
//
//        return aoc.getInvokableOrNull(peekIndex)
//    }

    /** @return next invokable index in Deck, -1 if non */
    fun peekNextIndex(startFrom: Int = 0): Int {
        val start = max(startFrom, data.deck.countTrailingZeroBits())
        return aoc.nextInvokableIndex(start)
    }
    fun peekNext(startFrom: Int = 0): AbstractCypher? {
        val start = max(startFrom, data.deck.countTrailingZeroBits())
        return aoc.nextInvokable(start)
    }

    /** non-empty-cypher, null if deck is empty */
    fun drawNext(): AbstractCypher? {
        // find the index of the first '1' (last '0', in fact) starting from the right.
        val drawIndex = data.deck.countTrailingZeroBits()
        if (drawIndex >= aoc.invokableSize) return null
        return draw(drawIndex)
    }

    /** non-empty */
    private fun draw(index: Int): AbstractCypher? {
        val cy = aoc[index]
        CypherNexus.debugCypher { "draw [$cy], the ${index + 1}th cypher" }
        if (cy.isEmpty()) {
            // this should not happen
            CypherNexus.LOGGER.error("draw [empty]: $index in $aoc, this should not happen")
            return null
        }
        if (data.manaCurrent < cy.manaDrain) {
            CypherNexus.debugCypher { "mana not enough, [$cy] discards directly" }
            deck2discard(index)
            return drawNext()
        }
        deck2hand(index)
        data.manaCurrent -= cy.manaDrain
        return cy
    }

    fun wrap() = run {
        if (data.discard > 0) CypherNexus.debugCypher { "discard ${data.discard.toString(2).padStart(8, '0')} wrap back into deck" }
        else CypherNexus.debugCypher { "discard is empty, nothing to wrap" }
        states.wrapped = true
        discard2deck()
    }

    fun reload() {
        data.deck = 0
        data.hand = 0
        data.discard = 0
    }

    fun init() {
        data.deck = aoc.bits() // really?
        data.hand = 0
        data.discard = 0
    }

    // ============== bit operations ===================================================

    private fun deck2hand(index: Int): AbstractCypher? {
        val cy = aoc.getInvokableOrNull(index)
        if (cy != null) {
            data.deck = data.deck and (1L shl index).inv()
            data.hand = data.hand or (1L shl index)
        }
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
        CypherNexus.debugCypher { "[$cy] discard from deck" }
    }
    /** discard next invokable */
    fun deckNext2discard(start: Int = 0) {
        val next = (data.deck shr start).countTrailingZeroBits() + start
        deck2discard(next)
    }
    /** batch discard: from <= ... < until */
    fun deck2discard(from: Int, until: Int) {
        val filter = ((1L shl from) - 1).inv()
        val filter1 = ((1L shl until) - 1) and filter
        val toDiscard = data.deck and filter1

//        CypherNexus.LOGGER.debugCypher("deck before bunch-discard: {}", data.deck.toString(2).padStart(20, '0'))

        data.deck = data.deck and toDiscard.inv()
        data.discard = data.discard or toDiscard

//        CypherNexus.LOGGER.debugCypher("deck after bunch-discard: {}", data.deck.toString(2).padStart(20, '0'))

        for (i in toDiscard.countTrailingZeroBits() until 64 - toDiscard.countLeadingZeroBits()) {
            val cy = aoc[i]
            if (cy.isInvokable()) CypherNexus.debugCypher { "[$cy] batch-discard from deck" }
        }
    }
    /** aka. wrap, return true if there is something to wrap */
    private fun discard2deck(): Boolean {
        val old = data.discard
        data.deck = data.deck or data.discard
        data.discard = 0
        return old > 0
    }
    // =================================================================================


    data class HelperDataBundle (
        var draw: Int,
        var delay: Int,
        var recharge: Int,
        var manaCurrent: Float,
        /** R -> L, invokable only */
        var hand: Long = 0,
        var deck: Long = 0,
        var discard: Long = 0,
    ) {
    }

    /** for special data persist along the invoking */
    data class InvokingStateBundle (
        var wrapped: Boolean = false,
        var alreadyRefreshed: Boolean = false,

        var drawEnabled: Boolean = true,

        var divideByChainLength: Int = 0,
        var divideByChainLengthMax: Int = 0,
    ) {
        // val map = ... // attach additional data if desire
    }
}