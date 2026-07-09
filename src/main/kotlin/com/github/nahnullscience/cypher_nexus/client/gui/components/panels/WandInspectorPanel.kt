package com.github.nahnullscience.cypher_nexus.client.gui.components.panels

import com.github.nahnullscience.cypher_nexus.client.gui.components.DragController
import com.github.nahnullscience.cypher_nexus.client.gui.components.IScreenRect
import com.github.nahnullscience.cypher_nexus.client.gui.components.RectBasics
import com.github.nahnullscience.cypher_nexus.client.gui.components.RenderConstants.ICON_SIZE
import com.github.nahnullscience.cypher_nexus.client.gui.components.RenderConstants.ITEM_SIZE
import com.github.nahnullscience.cypher_nexus.client.gui.components.RenderConstants.PADDING
import com.github.nahnullscience.cypher_nexus.client.gui.components.RenderConstants.WAND_BLOCK_MARGIN
import com.github.nahnullscience.cypher_nexus.client.gui.components.RenderConstants.renderCypherIcon
import com.github.nahnullscience.cypher_nexus.client.gui.components.UiEventBus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.wand.IWandLike
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.world.item.ItemStack
import kotlin.math.max

class WandInspectorPanel(
    val screen: Screen,
    val wands: List<ItemStack>,
    val bus: UiEventBus,
    val drag: DragController,
    private val rectLayout: IScreenRect = RectBasics()
) : IScreenRect by rectLayout, IScreenPanel {
    override fun extractRenderState(
        graphics: GuiGraphicsExtractor,
        mouseX: Int,
        mouseY: Int,
        partial: Float
    ) {
        if (wands.isNotEmpty()) {
            renderWandData(graphics, mouseX, mouseY, partial)
        }
    }

    fun renderWandData(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partial: Float) {
        val anchorX = x + WAND_BLOCK_MARGIN
        val anchorY1 = WAND_BLOCK_MARGIN
        val anchorY2 = anchorY1 + 60
        val cols = max(1, (x - 2 * WAND_BLOCK_MARGIN) / ITEM_SIZE)

        graphics.fill(anchorX, anchorY1, right - WAND_BLOCK_MARGIN, bot - WAND_BLOCK_MARGIN, 0xCC333333.toInt())

        val currentStack = wands[0]
        graphics.item(currentStack, anchorX, anchorY1)

        val wand = currentStack.item as? IWandLike ?: return
        val data = wand.getWandData(currentStack, null) ?: return

        val (manaMax, manaRegen) = data.invariable.chunkF
        val (draw, castDelay, rechargeTime) = data.invariable.chunkI
        val aoc = data.highPayload.aoc
        for (i in 0 until aoc.capacity) {
            val col = i % cols
            val row = i / cols
            val x = anchorX + PADDING + col * ITEM_SIZE
            val y = anchorY2 + PADDING + (row * ITEM_SIZE)
            val cypher = aoc[i]

            renderWandBlocks(graphics, cypher, x, y)
            renderCypherIcon(graphics, cypher, x, y)
        }
    }

    private fun renderWandBlocks(graphics: GuiGraphicsExtractor, cypher: AbstractCypher, x: Int, y: Int) {
        graphics.fill(x, y, x + ICON_SIZE, y + ICON_SIZE, 0xFF444444.toInt()) // bg
    }
}