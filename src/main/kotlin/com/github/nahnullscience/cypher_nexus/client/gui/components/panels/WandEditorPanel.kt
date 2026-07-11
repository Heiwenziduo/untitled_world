package com.github.nahnullscience.cypher_nexus.client.gui.components.panels

import com.github.nahnullscience.cypher_nexus.client.gui.components.DragController
import com.github.nahnullscience.cypher_nexus.client.gui.components.IScreenRect
import com.github.nahnullscience.cypher_nexus.client.gui.components.IconGrid
import com.github.nahnullscience.cypher_nexus.client.gui.components.RectBasics
import com.github.nahnullscience.cypher_nexus.client.gui.others.Hit
import com.github.nahnullscience.cypher_nexus.client.gui.others.RenderConstants.DARK
import com.github.nahnullscience.cypher_nexus.client.gui.others.RenderConstants.ELEMENT_PADDING
import com.github.nahnullscience.cypher_nexus.client.gui.others.RenderConstants.ELEMENT_SIZE
import com.github.nahnullscience.cypher_nexus.client.gui.others.RenderConstants.WAND_BLOCK_MARGIN
import com.github.nahnullscience.cypher_nexus.client.gui.others.RenderConstants.renderCypherHoverLayer
import com.github.nahnullscience.cypher_nexus.client.gui.others.RenderConstants.renderCypherIcon
import com.github.nahnullscience.cypher_nexus.client.gui.others.UiEvent
import com.github.nahnullscience.cypher_nexus.client.gui.others.UiEventBus
import com.github.nahnullscience.cypher_nexus.mechanic.wand.IWandLike
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.world.item.ItemStack
import kotlin.math.max

class WandEditorPanel(
    val screen: Screen,
    val wands: List<ItemStack>,
    val bus: UiEventBus,
    val drag: DragController,
    private val rectLayout: IScreenRect = RectBasics()
) : IScreenRect by rectLayout, IScreenPanel {

    private val grid = IconGrid()
    /**
     * [Hit.cypher] maybe Empty, check before use
     * */
    private var hovered: Hit? = null

    override fun resize(screenX: Int, screenY: Int) {
        rectLayout.resize(screenX, screenY)
        grid.cols = max(1, (w - 2 * WAND_BLOCK_MARGIN) / ELEMENT_SIZE)
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

        if (wands.isNotEmpty()) {
            renderWandData(graphics, mouseX, mouseY, partial)
        }
    }

    fun renderWandData(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partial: Float) {
        val anchorX = x + WAND_BLOCK_MARGIN
        val anchorY1 = y + WAND_BLOCK_MARGIN

        graphics.fill(anchorX, anchorY1, right - WAND_BLOCK_MARGIN, bot - WAND_BLOCK_MARGIN, DARK)

        val currentStack = wands[0]
        val wand = currentStack.item as? IWandLike ?: return
        val data = wand.getWandData(currentStack, null) ?: return

        val (manaMax, manaRegen) = data.invariable.chunkF
        val (draw, castDelay, rechargeTime) = data.invariable.chunkI
        val aoc = data.highPayload.aoc

        graphics.item(currentStack, anchorX, anchorY1)

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
        when (event.button()) {
            1 -> bus.emit(UiEvent.CypherActivated(hit.cypher, hit.rect))
            0 -> bus.emit(UiEvent.DragStarted(hit.cypher, hit.rect))
        }
        return true
    }

    private fun hitTest(mouseX: Double, mouseY: Double): Hit? {
        if (!contains(mouseX, mouseY)) return null

        val currentStack = wands[0]
        val wand = currentStack.item as? IWandLike ?: return null
        val data = wand.getWandData(currentStack, null) ?: return null
        val aoc = data.highPayload.aoc

        val index = grid.indexAt(mouseX.toInt(), mouseY.toInt()) ?: return null
        val cypher = aoc.getOrNull(index) ?: return null

        return Hit(cypher, index, grid.cellRect(index))
    }
}