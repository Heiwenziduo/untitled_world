package com.github.nahnullscience.cypher_nexus.client.gui.components.panels

import com.github.nahnullscience.cypher_nexus.client.gui.components.DragController
import com.github.nahnullscience.cypher_nexus.client.gui.components.IScreenRect
import com.github.nahnullscience.cypher_nexus.client.gui.components.RectBasics
import com.github.nahnullscience.cypher_nexus.client.gui.components.Scrollbar
import com.github.nahnullscience.cypher_nexus.client.gui.components.RenderConstants.CATEGORY_TITLE_PADDING
import com.github.nahnullscience.cypher_nexus.client.gui.components.RenderConstants.DARK
import com.github.nahnullscience.cypher_nexus.client.gui.components.RenderConstants.ICON_SIZE
import com.github.nahnullscience.cypher_nexus.client.gui.components.RenderConstants.ELEMENT_SIZE
import com.github.nahnullscience.cypher_nexus.client.gui.components.RenderConstants.MARGIN
import com.github.nahnullscience.cypher_nexus.client.gui.components.RenderConstants.PADDING
import com.github.nahnullscience.cypher_nexus.client.gui.components.RenderConstants.SCROLLBAR_WIDTH
import com.github.nahnullscience.cypher_nexus.client.gui.components.RenderConstants.WHITE
import com.github.nahnullscience.cypher_nexus.client.gui.components.RenderConstants.renderCypherHoverLayer
import com.github.nahnullscience.cypher_nexus.client.gui.components.RenderConstants.renderCypherIcon
import com.github.nahnullscience.cypher_nexus.client.gui.components.UiEvent
import com.github.nahnullscience.cypher_nexus.client.gui.components.UiEventBus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.category.CypherCategory
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.MouseButtonEvent
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
        cypherMap.entries.filter { it.value.isNotEmpty() } // drop empty categories up front
            .withIndex().map { (i, e) -> CategoryBlock(e.key, e.value, i) }

    private var cols: Int = 1
    private val scrollbar = Scrollbar()
    private var hovered: AbstractCypher? = null

    private val totalContentHeight: Int
        get() = blocks.sumOf { it.height } + CATEGORY_TITLE_PADDING

    override fun resize(screenX: Int, screenY: Int) {
        rectLayout.resize(screenX, screenY)
        cols = max(1, (w - MARGIN * 2) / ELEMENT_SIZE)
        blocks.forEach { it.resize() }
        scrollbar.layout(
            rect = ScreenRectangle(x, y, SCROLLBAR_WIDTH, h),
            viewportHeight = h,
            contentHeight = totalContentHeight
        )
    }

    override fun extractRenderState(
        graphics: GuiGraphicsExtractor,
        mouseX: Int,
        mouseY: Int,
        partial: Float
    ) {
        graphics.fill(x, y, x + w, y + h, DARK)

        // computed once per frame here — read by both the highlight below and by click handling,
        // never rediscovered mid-draw
        hovered = hitTest(mouseX.toDouble(), mouseY.toDouble())?.cypher

        for (b in blocks) renderCypherGrid(graphics, b)
        scrollbar.render(graphics)
    }

    private fun renderCypherGrid(graphics: GuiGraphicsExtractor, block: CategoryBlock) {
        val reY = y + block.reY - scrollbar.offset.toInt()
        val reX = x + MARGIN + SCROLLBAR_WIDTH

        graphics.text(screen.font, block.title, reX, reY - 12, WHITE)
        for ((index, cypher) in block.list.withIndex()) {
            val col = index % cols
            val row = index / cols
            val drawX = reX + col * ELEMENT_SIZE
            val drawY = reY + PADDING + (row * ELEMENT_SIZE)

            if (drawY + ICON_SIZE > y && drawY < y + h) {
                renderCypherIcon(graphics, cypher, drawX, drawY)
                if (cypher === hovered) {
                    renderCypherHoverLayer(graphics, drawX, drawY)
                }
            }
        }
    }

    // ================================================================================
    // input — scrollbar gets first refusal, then the grid itself
    // ================================================================================

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        if (!contains(mouseX, mouseY)) return false
        scrollbar.scrollBy(scrollY, ELEMENT_SIZE)
        return true
    }

    override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
        if (scrollbar.mouseClicked(event.x, event.y)) return true

        val hit = hitTest(event.x, event.y) ?: return false
        when (event.button()) {
            1 -> bus.emit(UiEvent.CypherActivated(hit.cypher, hit.rect)) // right-click quick-assign hook
            0 -> bus.emit(UiEvent.DragStarted(hit.cypher, hit.rect))
        }
        return true
    }

    override fun mouseDragged(event: MouseButtonEvent, dx: Double, dy: Double): Boolean =
        scrollbar.mouseDragged(event.y)

    override fun mouseReleased(event: MouseButtonEvent): Boolean =
        scrollbar.mouseReleased()

    // ================================================================================
    // hit-testing: pure inversion of the paint math above — O(#categories), no per-icon list
    // ================================================================================

    private data class Hit(val cypher: AbstractCypher, val rect: ScreenRectangle)

    private fun hitTest(mouseX: Double, mouseY: Double): Hit? {
        if (!contains(mouseX, mouseY)) return null

        val localY = (mouseY - y).toInt() + scrollbar.offset.toInt()
        val localX = (mouseX - x).toInt()

        val block = blocks.firstOrNull { localY >= it.reY && localY < it.reY + it.height } ?: return null
        val row = (localY - block.reY - PADDING) / ELEMENT_SIZE
        val col = (localX - MARGIN) / ELEMENT_SIZE
        if (col !in 0 until cols || row < 0) return null

        val cypher = block.list.getOrNull(row * cols + col)?.takeIf { it.isNotEmpty() } ?: return null
        val rect = ScreenRectangle(
            x + MARGIN + col * ELEMENT_SIZE,
            y + block.reY - scrollbar.offset.toInt() + PADDING + row * ELEMENT_SIZE,
            ICON_SIZE, ICON_SIZE
        )
        return Hit(cypher, rect)
    }

    private inner class CategoryBlock(
        val category: CypherCategory,
        val list: List<AbstractCypher>,
        val index: Int
    ) {
        val title: MutableComponent = category.translation()

        var reY: Int = 0
        var rows: Int = 1
        var height: Int = 0

        fun resize() {
            rows = (list.size + cols - 1) / cols // ceiling division — was silently adding a spare row
            height = rows * ELEMENT_SIZE + CATEGORY_TITLE_PADDING
            reY = blocks.filter { it.index < index }.sumOf { it.height } + CATEGORY_TITLE_PADDING
        }
    }
}