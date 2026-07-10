package com.github.nahnullscience.cypher_nexus.client.gui.components.panels

import com.github.nahnullscience.cypher_nexus.client.gui.components.*
import com.github.nahnullscience.cypher_nexus.client.gui.components.RenderConstants.DARK
import com.github.nahnullscience.cypher_nexus.client.gui.components.RenderConstants.ELEMENT_SIZE
import com.github.nahnullscience.cypher_nexus.client.gui.components.RenderConstants.ICON_SIZE
import com.github.nahnullscience.cypher_nexus.client.gui.components.RenderConstants.PADDING
import com.github.nahnullscience.cypher_nexus.client.gui.components.RenderConstants.WAND_BLOCK_MARGIN
import com.github.nahnullscience.cypher_nexus.client.gui.components.RenderConstants.renderCypherHoverLayer
import com.github.nahnullscience.cypher_nexus.client.gui.components.RenderConstants.renderCypherIcon
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.wand.IWandLike
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.world.item.ItemStack
import kotlin.math.max

class WandInspectorPanel(
    val screen: Screen,
    val wands: List<ItemStack>,
    val bus: UiEventBus,
    val drag: DragController,
    private val rectLayout: IScreenRect = RectBasics()
) : IScreenRect by rectLayout, IScreenPanel {

    private var cols: Int = 1
    private var hovered: AbstractCypher? = null

    override fun resize(screenX: Int, screenY: Int) {
        rectLayout.resize(screenX, screenY)
        cols = max(1, (x - 2 * WAND_BLOCK_MARGIN) / ELEMENT_SIZE)
    }

    override fun extractRenderState(
        graphics: GuiGraphicsExtractor,
        mouseX: Int,
        mouseY: Int,
        partial: Float
    ) {
        hovered = hitTest(mouseX.toDouble(), mouseY.toDouble())?.cypher

        if (wands.isNotEmpty()) {
            renderWandData(graphics, mouseX, mouseY, partial)
        }
    }

    fun renderWandData(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partial: Float) {
        val anchorX = x + WAND_BLOCK_MARGIN
        val anchorY1 = y + WAND_BLOCK_MARGIN
        val anchorY2 = anchorY1 + 60

        graphics.fill(anchorX, anchorY1, right - WAND_BLOCK_MARGIN, bot - WAND_BLOCK_MARGIN, DARK)

        val currentStack = wands[0]
        val wand = currentStack.item as? IWandLike ?: return
        val data = wand.getWandData(currentStack, null) ?: return

        val (manaMax, manaRegen) = data.invariable.chunkF
        val (draw, castDelay, rechargeTime) = data.invariable.chunkI
        val aoc = data.highPayload.aoc

        graphics.item(currentStack, anchorX, anchorY1)
        for (i in 0 until aoc.capacity) {
            val col = i % cols
            val row = i / cols
            val x = anchorX + PADDING + col * ELEMENT_SIZE
            val y = anchorY2 + PADDING + (row * ELEMENT_SIZE)
            val cypher = aoc[i]

            graphics.fill(x, y, x + ICON_SIZE, y + ICON_SIZE, 0xFF444444.toInt()) // bg
            renderCypherIcon(graphics, cypher, x, y)
            if (cypher === hovered) {
                renderCypherHoverLayer(graphics, x, y)
            }
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

    private data class Hit(val cypher: AbstractCypher, val rect: ScreenRectangle)
    private fun hitTest(mouseX: Double, mouseY: Double): Hit? {
        if (!contains(mouseX, mouseY)) return null

        val localY = (mouseY - y).toInt()
        val localX = (mouseX - x).toInt()

        val anchorX = x + WAND_BLOCK_MARGIN
        val anchorY = y + WAND_BLOCK_MARGIN + 60

        val row = (localY - anchorY - PADDING) / ELEMENT_SIZE
        val col = (localX - anchorX - PADDING) / ELEMENT_SIZE
        if (col !in 0 until cols || row < 0) return null

        val currentStack = wands[0]
        val wand = currentStack.item as? IWandLike ?: return null
        val data = wand.getWandData(currentStack, null) ?: return null
        val aoc = data.highPayload.aoc

        val cypher = aoc[row * cols + col].takeIf { it.isNotEmpty() } ?: return null
        val rect = ScreenRectangle(
            anchorX + PADDING + col * ELEMENT_SIZE,
            anchorY + PADDING + row * ELEMENT_SIZE,
            ICON_SIZE, ICON_SIZE
        )
        return Hit(cypher, rect)
    }
}