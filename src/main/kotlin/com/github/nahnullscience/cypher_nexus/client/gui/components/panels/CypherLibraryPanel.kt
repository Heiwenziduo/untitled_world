package com.github.nahnullscience.cypher_nexus.client.gui.components.panels

import com.github.nahnullscience.cypher_nexus.client.gui.components.DragController
import com.github.nahnullscience.cypher_nexus.client.gui.components.IScreenRect
import com.github.nahnullscience.cypher_nexus.client.gui.components.RectBasics
import com.github.nahnullscience.cypher_nexus.client.gui.components.RenderConstants.CATEGORY_TITLE_PADDING
import com.github.nahnullscience.cypher_nexus.client.gui.components.RenderConstants.DARK
import com.github.nahnullscience.cypher_nexus.client.gui.components.RenderConstants.ICON_SIZE
import com.github.nahnullscience.cypher_nexus.client.gui.components.RenderConstants.ITEM_SIZE
import com.github.nahnullscience.cypher_nexus.client.gui.components.RenderConstants.MARGIN
import com.github.nahnullscience.cypher_nexus.client.gui.components.RenderConstants.PADDING
import com.github.nahnullscience.cypher_nexus.client.gui.components.RenderConstants.WHITE
import com.github.nahnullscience.cypher_nexus.client.gui.components.RenderConstants.renderCypherIcon
import com.github.nahnullscience.cypher_nexus.client.gui.components.UiEventBus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.category.CypherCategory
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.MutableComponent
import kotlin.math.max

class CypherLibraryPanel(
    val screen: Screen,
    val cypherMap: Map<CypherCategory, List<AbstractCypher>>,
    val bus: UiEventBus,
    val drag: DragController,
    private val rectLayout: IScreenRect = RectBasics()
) : IScreenRect by rectLayout, IScreenPanel {

    private val blocks: List<CategoryBlock> =
        cypherMap.entries.withIndex().map { (i, e) -> CategoryBlock(e.key, e.value, i) }

    private var cols: Int = 1

    override fun resize(screenX: Int, screenY: Int) {
        rectLayout.resize(screenX, screenY)
        cols = max(1, (w - MARGIN * 2) / ITEM_SIZE)
        blocks.forEach { block -> block.resize() }
    }

    override fun extractRenderState(
        graphics: GuiGraphicsExtractor,
        mouseX: Int,
        mouseY: Int,
        partial: Float
    ) {
        graphics.outline(x, y, w, h, 0xffffffff.toInt())
        graphics.fill(x, y, x + w, y + h, DARK)
        for (b in blocks) {
            renderCypherGrid(graphics, mouseX, mouseY, partial, b)
        }
    }

    private fun renderCypherGrid(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partial: Float, block: CategoryBlock) {
        if (!block.show) return
        val reY = block.reY

        // render category title
        graphics.text(screen.font, block.title, MARGIN, reY - 12, WHITE)

        // draw cyphers
        for ((index, cypher) in block.list.withIndex()) {
            val col = index % cols
            val row = index / cols
            val x = MARGIN + col * ITEM_SIZE
            val y = reY + PADDING + (row * ITEM_SIZE)

            if (y + ICON_SIZE > 0 && y < h) {
                renderCypherIcon(graphics, cypher, x, y)

            }
        }
    }

    private inner class CategoryBlock(
        val category: CypherCategory,
        val list: List<AbstractCypher>,
        val index: Int
    ) {
        val title: MutableComponent = category.translation()
        val show: Boolean = list.isNotEmpty()

        var reY: Int = 0
        var rows: Int = 1
        var height: Int = 0

        fun resize() {
            rows = list.size / cols + 1
            height = rows * ITEM_SIZE + CATEGORY_TITLE_PADDING
            reY = blocks.filter { it.index < index }.sumOf { it.height } + CATEGORY_TITLE_PADDING
        }
    }
}