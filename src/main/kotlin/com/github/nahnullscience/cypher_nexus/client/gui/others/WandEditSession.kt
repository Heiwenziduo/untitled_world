package com.github.nahnullscience.cypher_nexus.client.gui.others

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.EmptyCypher
import com.github.nahnullscience.cypher_nexus.mechanic.wand.IItemWand
import com.github.nahnullscience.cypher_nexus.mechanic.wand.IItemWand.Companion.getInvokingRecipe
import com.github.nahnullscience.cypher_nexus.mechanic.wand.IItemWand.Companion.getWandData
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.ItemWandDataInvariable
import com.github.nahnullscience.cypher_nexus.network.server.ServerboundEditWandCyphers
import com.github.nahnullscience.cypher_nexus.utility.mod.ArrayOfCyphers.MutableAoC
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap
import net.minecraft.client.Minecraft
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.client.network.ClientPacketDistributor
import java.util.UUID

/**
 * owns wand-editing state: which wand is open, its working (possibly edited) cypher array,
 * and a queue of edits for wands the player switched away from without committing.
 *
 * knows nothing about pixels, mouse input, or GuiGraphics — `WandEditorPanel` is the only
 * thing that calls into this, and it's the only thing this class reports back to.
 * */
class WandEditSession(private val wands: List<ItemStack>) {

    /** uuid -> edited cypher list, for wands touched this session but not yet flushed */
    private val pendingEdits = Object2ReferenceOpenHashMap<UUID, MutableAoC>(8).also { it.defaultReturnValue(null) }

    var selectedIndex = 0
        private set

    private var workingCopy: MutableAoC? = null
    private var workingUuid: UUID? = null
    private var dirty = false

    val wandCount get() = wands.size
    val currentStack: ItemStack? get() = wands.getOrNull(selectedIndex)
    val currentAoc: MutableAoC? get() = workingCopy
    val currentInvariable: ItemWandDataInvariable? get() = currentStack?.getWandData()

    init {
        if (wands.isNotEmpty()) {
            val localPlayer = Minecraft.getInstance().player!!
            for ((i, stack) in wands.withIndex()) {
                if (localPlayer.mainHandItem == stack) {
                    selectedIndex = i // not a data class so this equality check could be unreliable.
                    break
                } else if (localPlayer.offhandItem == stack) {
                    selectedIndex = i
                }
            }
            loadSelected()
        }
    }

    fun selectWand(index: Int) {
        if (index !in wands.indices || index == selectedIndex) return
        stashCurrentEdits()
        selectedIndex = index
        loadSelected()
    }

    fun selectNext() = selectWand((selectedIndex + 1) % wandCount)
    fun selectPrevious() = selectWand((selectedIndex - 1 + wandCount) % wandCount)

    /** mutate one slot of whichever wand is currently open */
    fun setSlot(index: Int, cypher: AbstractCypher?) {
        val copy = workingCopy ?: return
        if (index !in 0 until copy.capacity) return
        val target = cypher ?: EmptyCypher
        if (copy[index] === target) return
        copy[index] = cypher
        dirty = true
    }

    private fun loadSelected() {
        val stack = wands.getOrNull(selectedIndex)
        val data = stack?.getWandData()
        val aoc = stack?.getInvokingRecipe()

        if (data == null || aoc == null) {
            workingCopy = null; workingUuid = null; dirty = false
            return
        }

        val uuid = data.uuid
        workingUuid = uuid
        // resume an edit made earlier this session, otherwise start from the stack's real data
        workingCopy = pendingEdits[uuid]?.let { MutableAoC(it) } ?: aoc.toMutable()
        dirty = false
    }

    private fun stashCurrentEdits() {
        if (!dirty) return // don't queue a no-op packet for wands the player only looked at
        val uuid = workingUuid ?: return
        val copy = workingCopy ?: return
        pendingEdits[uuid] = copy
        dirty = false
    }

    /** call on screen close (or a future "apply" button) — flushes every touched wand */
    fun commitAll() {
        stashCurrentEdits()
        pendingEdits.forEach { (uuid, cyphers) ->
            ClientPacketDistributor.sendToServer(ServerboundEditWandCyphers(uuid, cyphers))
        }
        pendingEdits.clear()
    }
}