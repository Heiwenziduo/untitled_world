package com.github.nahnullscience.cypher_nexus.client.gui.components.panels

import com.github.nahnullscience.cypher_nexus.client.gui.components.DragController
import com.github.nahnullscience.cypher_nexus.client.gui.components.IScreenRect
import com.github.nahnullscience.cypher_nexus.client.gui.components.IconGrid
import com.github.nahnullscience.cypher_nexus.client.gui.components.RectBasics
import com.github.nahnullscience.cypher_nexus.client.gui.others.Hit
import com.github.nahnullscience.cypher_nexus.client.gui.others.IndexScreenEvents.*
import com.github.nahnullscience.cypher_nexus.client.gui.others.RenderConstants.DARK
import com.github.nahnullscience.cypher_nexus.client.gui.others.RenderConstants.ELEMENT_PADDING
import com.github.nahnullscience.cypher_nexus.client.gui.others.RenderConstants.WAND_BLOCK_MARGIN
import com.github.nahnullscience.cypher_nexus.client.gui.others.RenderConstants.renderCypherHoverLayer
import com.github.nahnullscience.cypher_nexus.client.gui.others.RenderConstants.renderCypherIcon
import com.github.nahnullscience.cypher_nexus.client.gui.others.UiEventBus
import com.github.nahnullscience.cypher_nexus.client.gui.others.WandEditSession
import com.github.nahnullscience.cypher_nexus.utility.mod.ArrayOfCyphers
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.world.item.ItemStack
import kotlin.math.max

class WandEditorPanel(
    val screen: Screen,
    wands: List<ItemStack>,
    val bus: UiEventBus,
    val drag: DragController,
    private val rectLayout: IScreenRect = RectBasics()
) : IScreenRect by rectLayout, IScreenPanel {

    private val session = WandEditSession(wands)
    private val grid = IconGrid()
    /**
     * [Hit.cypher] maybe Empty, check before use
     * */
    private var hovered: Hit? = null

    init {
        // right-click quick-assign from the library — session owns the mutation, panel just relays
        bus.subscribe { event ->
            if (event is CypherQuickAssign) {
                val aoc = session.currentAoc ?: return@subscribe
                val slot = aoc.firstEmptyIndex().takeIf { it >= 0 } ?: return@subscribe
                session.setSlot(slot, event.cypher)
                bus.emit(WandSlotAssigned(event.cypher, grid.cellRect(slot), event.fromRect))
            }
        }
    }

    override fun resize(screenX: Int, screenY: Int) {
        rectLayout.resize(screenX, screenY)
        grid.cols = max(1, (w - 2 * WAND_BLOCK_MARGIN) / grid.elementSize)
        grid.originX = x + WAND_BLOCK_MARGIN + ELEMENT_PADDING
        grid.originY = y + WAND_BLOCK_MARGIN + 60
    }

    override fun extractRenderState(
        graphics: GuiGraphicsExtractor,
        mouseX: Int,
        mouseY: Int,
        partial: Float
    ) {
        hovered = hitTest(mouseX.toDouble(), mouseY.toDouble())

        val stack = session.currentStack ?: return
        val aoc = session.currentAoc ?: return
        renderWandData(graphics, stack, aoc)
    }

    private fun renderWandData(graphics: GuiGraphicsExtractor, stack: ItemStack, aoc: ArrayOfCyphers) {
        val anchorX = x + WAND_BLOCK_MARGIN
        val anchorY1 = y + WAND_BLOCK_MARGIN

        graphics.fill(anchorX, anchorY1, right - WAND_BLOCK_MARGIN, bot - WAND_BLOCK_MARGIN, DARK)
        graphics.item(stack, anchorX, anchorY1) // TODO: click this to session.selectNext(), or swap for a real wand selector

        for (i in 0 until aoc.capacity) {
            val rect = grid.cellRect(i)
            val cypher = aoc[i]
            graphics.fill(rect.left(), rect.top(), rect.right(), rect.bottom(), 0xFF444444.toInt())
            renderCypherIcon(graphics, cypher, rect.left(), rect.top())
            if (i == hovered?.index) renderCypherHoverLayer(graphics, rect.left(), rect.top())
        }
    }

    override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
        val hit = hitTest(event.x, event.y) ?: return false
        if (hit.cypher.isEmpty()) return false   // nothing to pick up or delete on a blank slot

        when (event.button()) {
            0 -> bus.emit(DragStarted(hit.cypher, hit.rect))
            1 -> session.setSlot(hit.index, null)
        }
        return true
    }

    override fun mouseReleased(event: MouseButtonEvent): Boolean {
        val payload = drag.current ?: return false // no drag active, nothing to do
        val index = grid.indexAt(event.x.toInt(), event.y.toInt()) ?: return false
        session.setSlot(index, payload.cypher)
        return true
    }

    override fun onScreenClose() = session.commitAll()

    private fun hitTest(mouseX: Double, mouseY: Double): Hit? {
        if (!contains(mouseX, mouseY)) return null
        val aoc = session.currentAoc ?: return null
        val index = grid.indexAt(mouseX.toInt(), mouseY.toInt()) ?: return null
        if (index !in 0 until aoc.capacity) return null
        return Hit(aoc[index], index, grid.cellRect(index))
    }
}