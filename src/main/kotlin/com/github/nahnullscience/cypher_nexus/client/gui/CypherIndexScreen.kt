package com.github.nahnullscience.cypher_nexus.client.gui

import com.github.nahnullscience.cypher_nexus.client.gui.components.AnimationController
import com.github.nahnullscience.cypher_nexus.client.gui.components.DragController
import com.github.nahnullscience.cypher_nexus.client.gui.others.RenderConstants.HEADER_HEIGHT
import com.github.nahnullscience.cypher_nexus.client.gui.others.RenderConstants.WHITE
import com.github.nahnullscience.cypher_nexus.client.gui.others.UiEventBus
import com.github.nahnullscience.cypher_nexus.client.gui.components.panels.CypherLibraryPanel
import com.github.nahnullscience.cypher_nexus.client.gui.components.panels.HeaderMenuPanel
import com.github.nahnullscience.cypher_nexus.client.gui.components.panels.IScreenPanel
import com.github.nahnullscience.cypher_nexus.client.gui.components.panels.WandEditorPanel
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.category.CypherCategory
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import net.minecraft.sounds.Music
import net.minecraft.world.item.ItemStack

class CypherIndexScreen(
    val enabledCyphers: Map<CypherCategory, List<AbstractCypher>>,
    val wandList: List<ItemStack>
): Screen(Component.literal("Cypher Index")) {
    private val bus = UiEventBus()
    private val dragController = DragController(bus)
    private val animationController = AnimationController()

    private val menu = HeaderMenuPanel(this, bus, dragController)
    private val library = CypherLibraryPanel(this, enabledCyphers, bus, dragController)
    private val editor = WandEditorPanel(this, wandList, bus, dragController)
    private val panels = listOf(menu, library, editor)

    // whichever panel consumes mouseClicked keeps receiving drag/release, even once the
    // pointer leaves its bounds mid-drag (e.g. dragging a scrollbar thumb below the panel)
    private var capturedPanel: IScreenPanel? = null

    init {
        menu.setResizeFunction(
            { 0 },
            { 0 },
            { w -> w },
            { h -> HEADER_HEIGHT }
        )
        library.setResizeFunction(
            { 0 },
            { HEADER_HEIGHT },
            { w -> w / 2 },
            { h -> h - HEADER_HEIGHT }
        )
        editor.setResizeFunction(
            { x -> x / 2 },
            { HEADER_HEIGHT },
            { w -> w / 2 },
            { h -> h - HEADER_HEIGHT }
        )
    }

    override fun init() {
        super.init()
        panels.forEach { panel ->
            addRenderableOnly(panel) // widgets are rebuilt when resize
            panel.resize(width, height)
        }
    }

    override fun extractBackground(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        super.extractBackground(graphics, mouseX, mouseY, a)
//        panels.forEach { panel -> graphics.outline(panel.x, panel.y, panel.w, panel.h, WHITE) }
    }

    override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
        val target = panels.firstOrNull { it.contains(event.x, event.y) }
        if (target != null && target.mouseClicked(event, doubleClick)) {
            capturedPanel = target
            return true
        }
        return super.mouseClicked(event, doubleClick)
    }

    override fun mouseDragged(event: MouseButtonEvent, dx: Double, dy: Double): Boolean =
        capturedPanel?.mouseDragged(event, dx, dy) ?: super.mouseDragged(event, dx, dy)

    override fun mouseReleased(event: MouseButtonEvent): Boolean {
        val target = capturedPanel
        capturedPanel = null
        return target?.mouseReleased(event) ?: super.mouseReleased(event)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        val target = panels.firstOrNull { it.contains(mouseX, mouseY) }
        return target?.mouseScrolled(mouseX, mouseY, scrollX, scrollY) == true
                || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
    }

    override fun onClose() {
        super.onClose()
        panels.forEach { it.onScreenClose() }
    }

    override fun isPauseScreen() = false
    override fun getBackgroundMusic(): Music? = super.getBackgroundMusic()
}