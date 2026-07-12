package com.github.nahnullscience.cypher_nexus.client.gui.components.panels

import com.github.nahnullscience.cypher_nexus.client.gui.components.*
import com.github.nahnullscience.cypher_nexus.client.gui.others.Hit
import com.github.nahnullscience.cypher_nexus.client.gui.others.IndexScreenEvents.CypherQuickAssign
import com.github.nahnullscience.cypher_nexus.client.gui.others.IndexScreenEvents.DragStarted
import com.github.nahnullscience.cypher_nexus.client.gui.others.RenderConstants.DARK
import com.github.nahnullscience.cypher_nexus.client.gui.others.RenderConstants.ELEMENT_SIZE
import com.github.nahnullscience.cypher_nexus.client.gui.others.RenderConstants.SCROLLBAR_WIDTH
import com.github.nahnullscience.cypher_nexus.client.gui.others.RenderConstants.WHITE
import com.github.nahnullscience.cypher_nexus.client.gui.others.RenderConstants.renderCypherHoverLayer
import com.github.nahnullscience.cypher_nexus.client.gui.others.RenderConstants.renderCypherIcon
import com.github.nahnullscience.cypher_nexus.client.gui.others.RenderConstants.renderCypherTooltip
import com.github.nahnullscience.cypher_nexus.client.gui.others.UiEventBus
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

    companion object {
        private const val LIBRARY_MARGIN = 8
        private const val CATEGORY_TITLE_PADDING = 22
    }

    private val blocks: List<CategoryBlock> =
        cypherMap.entries.filter { it.value.isNotEmpty() } // drop empty categories up front
            .withIndex().map { (i, e) -> CategoryBlock(e.key, e.value, i) }

    private var cols: Int = 1
    private val scrollbar = Scrollbar()
    private var hovered: Hit? = null

    private val totalContentHeight: Int
        get() = blocks.sumOf { it.height } + CATEGORY_TITLE_PADDING

    override fun resize(screenX: Int, screenY: Int) {
        rectLayout.resize(screenX, screenY)
        cols = max(1, (w - LIBRARY_MARGIN * 2) / ELEMENT_SIZE)
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
        hovered = hitTest(mouseX.toDouble(), mouseY.toDouble())

        if (!drag.isDragging) hovered?.let { hit ->
            renderCypherTooltip(graphics, screen.font, hit.cypher, mouseX, mouseY)
        }

        graphics.enableScissor(x, y, x + w, y + h)
        blocks.forEach { block ->
            block.updateScroll()
            renderCypherGrid(graphics, block)
        }
        scrollbar.render(graphics)
        graphics.disableScissor()
    }

    private fun renderCypherGrid(graphics: GuiGraphicsExtractor, block: CategoryBlock) {
        val grid = block.grid
        val reX = grid.originX
        val reY = grid.originY

        graphics.text(screen.font, block.title, reX, reY - 14, WHITE)
        for ((i, cypher) in block.list.withIndex()) {
            val rect = grid.cellRect(i)
            renderCypherIcon(graphics, cypher, rect.left(), rect.top())
            if (cypher === hovered?.cypher) renderCypherHoverLayer(graphics, rect.left(), rect.top())
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
            0 -> bus.emit(DragStarted(hit.cypher, hit.rect))
            1 -> bus.emit(CypherQuickAssign(hit.cypher, hit.rect)) // right-click quick-assign hook
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

    private fun hitTest(mouseX: Double, mouseY: Double): Hit? {
        if (!contains(mouseX, mouseY)) return null

        val localY = (mouseY - y).toInt() + scrollbar.offset.toInt()

        val block = blocks.firstOrNull { localY >= it.reY && localY < it.reY + it.height } ?: return null
        val index = block.grid.indexAt(mouseX.toInt(), mouseY.toInt()) ?: return null
        val cypher = block.list.getOrNull(index)?.takeIf { it.isNotEmpty() } ?: return null

        return Hit(cypher, index, block.grid.cellRect(index))
    }

    private inner class CategoryBlock(
        val category: CypherCategory,
        val list: List<AbstractCypher>,
        val index: Int
    ) {
        val grid = IconGrid()
        val title: MutableComponent = category.translation()

        var reY: Int = 0
        var rows: Int = 1
        var height: Int = 0

        fun resize() {
            rows = (list.size + cols - 1) / cols // ceiling division — was silently adding a spare row
            height = rows * ELEMENT_SIZE + CATEGORY_TITLE_PADDING
            reY = blocks.filter { it.index < index }.sumOf { it.height } + CATEGORY_TITLE_PADDING

            grid.cols = cols
            grid.originX = x + LIBRARY_MARGIN + SCROLLBAR_WIDTH
            updateScroll()
        }

        fun updateScroll() {
            grid.originY = y + reY - scrollbar.offset.toInt()
        }
    }
}