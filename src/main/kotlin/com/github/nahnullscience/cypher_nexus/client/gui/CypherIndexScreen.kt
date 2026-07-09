package com.github.nahnullscience.cypher_nexus.client.gui

import com.github.nahnullscience.cypher_nexus.client.gui.components.AnimationController
import com.github.nahnullscience.cypher_nexus.client.gui.components.DragController
import com.github.nahnullscience.cypher_nexus.client.gui.components.RenderConstants.WHITE
import com.github.nahnullscience.cypher_nexus.client.gui.components.UiEventBus
import com.github.nahnullscience.cypher_nexus.client.gui.components.panels.CypherLibraryPanel
import com.github.nahnullscience.cypher_nexus.client.gui.components.panels.WandInspectorPanel
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.category.CypherCategory
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
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

    private val library = CypherLibraryPanel(this, enabledCyphers, bus, dragController)
    private val inspector = WandInspectorPanel(this, wandList, bus, dragController)
    private val panels = listOf(library, inspector)

    init {
        library.setResizeFunction(
            { x -> 0 },
            { y -> 0 },
            { w -> w / 2 },
            { h -> h },
        )

        inspector.setResizeFunction(
            { x -> x / 2 },
            { y -> 0 },
            { w -> w / 2 },
            { h -> h },
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
        panels.forEach { panel ->
            // show borders for debug
            graphics.outline(panel.x, panel.y, panel.w, panel.h, WHITE)
        }
    }

    override fun isPauseScreen() = false
    override fun getBackgroundMusic(): Music? = super.getBackgroundMusic()
}