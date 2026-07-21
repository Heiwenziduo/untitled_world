package com.github.nahnullscience.cypher_nexus.client.gui.others

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import java.util.Optional

object GUIConstants {
    val theme: ColorTheme get() = Classic
    const val WHITE = 0xFFFFFFFF.toInt()
    const val LIGHT = 0x33FFFFFF
    const val GREY = 0xCC777777.toInt()
    const val DARK = 0xCC333333.toInt()
    const val BLACK = 0xCC000000.toInt()

    const val HEADER_HEIGHT = 16

    const val ICON_TEXTURE = 12
    const val ICON_SIZE = 12
    const val ICON_BORDER = 1
    const val BORDER_SIZE = ICON_SIZE + ICON_BORDER * 2
    const val ICON_SIZE_HALF = ICON_SIZE / 2
    const val ELEMENT_PADDING = 3 // space between icons
    const val ELEMENT_SIZE = ICON_SIZE + ELEMENT_PADDING

    const val SCROLLBAR_WIDTH = 4

    val cypherBg = CypherNexus.modResource("textures/gui/cypher_bg.png")
    val cypherBgEmpty = CypherNexus.modResource("textures/gui/cypher_bg_empty.png")

    fun GuiGraphicsExtractor.renderCypherSlotBG(cypher: AbstractCypher, x: Int, y: Int) {
        fill(x, y, x + ICON_SIZE, y + ICON_SIZE, DARK)
        if (cypher.isEmpty())
        blit(
            RenderPipelines.GUI_TEXTURED,
            cypherBgEmpty,
            x - ICON_BORDER,
            y - ICON_BORDER,
            0.0f,
            0.0f,
            BORDER_SIZE,
            BORDER_SIZE,
            BORDER_SIZE,
            BORDER_SIZE,
            GREY
        )
    }

    fun GuiGraphicsExtractor.renderCypherIcon(cypher: AbstractCypher, x: Int, y: Int) {
        if (cypher.isNotEmpty()) {
            val borderColor = if (cypher.color != 0) cypher.color else cypher.category.value().color
            blit(
                RenderPipelines.GUI_TEXTURED,
                cypherBg,
                x - ICON_BORDER,
                y - ICON_BORDER,
                0.0f,
                0.0f,
                BORDER_SIZE,
                BORDER_SIZE,
                BORDER_SIZE,
                BORDER_SIZE,
                borderColor
            )
            blit(
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

    fun GuiGraphicsExtractor.renderCypherHoverLayer(x: Int, y: Int) {
        fill(x, y, x + ICON_SIZE, y + ICON_SIZE, LIGHT)
    }

    fun GuiGraphicsExtractor.renderCypherTooltip(font: Font, cypher: AbstractCypher, mouseX: Int, mouseY: Int) {
        if (cypher.isEmpty()) return
//        val components = mutableListOf<ClientTooltipComponent>()
//
//        val titleText = cypher.translation().withStyle(ChatFormatting.GOLD)
//        components.add(ClientTooltipComponent.create(titleText.visualOrderText))
//        val descriptionText = cypher.description().withStyle(ChatFormatting.GRAY)
////        components.add(CypherDescriptionTooltip(CypherDescriptionTooltip.TooltipDataBundle(descText, cypher.texture())))
//
//        for (c in cypher.attributesTooltip) {
//            components.add(ClientTooltipComponent.create(c.visualOrderText))
//        }
//
//        graphics.tooltip(font, components, mouseX, mouseY, DefaultTooltipPositioner.INSTANCE, null)

        val componentsList = mutableListOf<Component>()
        val titleText = cypher.translation().withStyle(ChatFormatting.GOLD)
        componentsList.add(titleText)
        componentsList.addAll(cypher.attributesTooltip)
        setTooltipForNextFrame(font, componentsList, Optional.empty(), mouseX, mouseY)
    }

    /**
     * draws a small square glyph immediately followed by [text] on the same line, vertically centered
     * against each other. returns the x-coordinate right after the text, so callers can chain several
     * icon+text pairs on one line if they want.
     *
     * deliberately generic about what "the icon" is — pass a lambda that draws whatever: a flat color
     * swatch, a real texture blit, or an existing helper like [renderCypherIcon]. that's what lets this
     * one primitive cover a stat readout today and a cypher-icon-prefixed tooltip line later, without
     * writing a second version of this function when that day comes.
     * */
    fun GuiGraphicsExtractor.renderIconText(
        font: Font,
        text: Component,
        x: Int,
        y: Int,
        glyphSize: Int = 8,
        gap: Int = 3,
        color: Int = WHITE,
        glyph: GuiGraphicsExtractor.(gx: Int, gy: Int, gSize: Int) -> Unit
    ): Int {
        val glyphY = y + (font.lineHeight - glyphSize) / 2 // center the glyph against the text's line height
        val textX = x + glyphSize + gap

        this.glyph(x, glyphY, glyphSize)
        this.text(font, text, textX, y, color)
        return textX + font.width(text)
    }
}