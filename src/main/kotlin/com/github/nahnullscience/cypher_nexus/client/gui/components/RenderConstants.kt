package com.github.nahnullscience.cypher_nexus.client.gui.components

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.renderer.RenderPipelines

object RenderConstants {
    val theme: ColorTheme get() = Classic
    const val WHITE = 0xFFFFFFFF.toInt()
    const val LIGHT = 0x33FFFFFF
    const val DARK = 0xCC333333.toInt()
    const val BLACK = 0xCC000000.toInt()

    const val ICON_TEXTURE = 12
    const val ICON_SIZE = 12
    const val ICON_SIZE_HALF = ICON_SIZE / 2
    const val MARGIN = 8 // space between content and border
    const val PADDING = 3 // space between icons
    const val ELEMENT_SIZE = ICON_SIZE + PADDING
    const val CATEGORY_TITLE_PADDING = 18

    const val SCROLLBAR_WIDTH = 4

    const val WAND_BLOCK_MARGIN = 20

    fun renderCypherIcon(graphics: GuiGraphicsExtractor, cypher: AbstractCypher, x: Int, y: Int) {
        if (cypher.isNotEmpty()) {
            val borderColor = if (cypher.color != 0) cypher.color else cypher.category.value().color
            graphics.outline(x - 1, y - 1, ICON_SIZE + 2, ICON_SIZE + 2, borderColor)
//            graphics.blit(cypher.texture(), x, y, x + ICON_SIZE, y + ICON_SIZE, 0f, 0f, 0f, 0f)
            graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                cypher.texture(),
                x,
                y,
                0.0f,
                0.0f,
                ICON_SIZE,
                ICON_SIZE,
                ICON_TEXTURE,
                ICON_TEXTURE
            )
        }
    }
    fun renderCypherHoverLayer(graphics: GuiGraphicsExtractor, x: Int, y: Int) {
        graphics.fill(x, y, x + ICON_SIZE, y + ICON_SIZE, LIGHT)
    }
}