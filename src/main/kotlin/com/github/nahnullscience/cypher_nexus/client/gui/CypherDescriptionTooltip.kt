package com.github.nahnullscience.cypher_nexus.client.gui

import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.inventory.tooltip.TooltipComponent
import org.joml.Matrix4f

/** special tooltip to put string and image in one line */
class CypherDescriptionTooltip (private val data: TooltipDataBundle) : ClientTooltipComponent {
    // Height is the max of the icon (16px) and text height, plus some padding
    override fun getHeight(): Int = 20

    // Total width: text width + icon width (16px) + gap between them (4px)
    override fun getWidth(font: Font): Int {
        return font.width(data.description) + 16 + 4
    }

    override fun renderImage(font: Font, mouseX: Int, mouseY: Int, guiGraphics: GuiGraphics) {
        val textWidth = font.width(data.description)
        // Draw icon to the right of the text
        guiGraphics.blit(data.icon, mouseX + textWidth + 4, mouseY + 2, 0f, 0f, 16, 16, 16, 16)
    }

    override fun renderText(
        font: Font,
        mouseX: Int,
        mouseY: Int,
        matrix: Matrix4f,
        bufferSource: MultiBufferSource.BufferSource
    ) {
        // Draw the text aligned to the left
        font.drawInBatch(
            data.description,
            mouseX.toFloat(),
            mouseY.toFloat() + 6, // Slightly lowered to vertically center with the 16x16 icon
            -1,
            true,
            matrix,
            bufferSource,
            Font.DisplayMode.NORMAL,
            0,
            15728880
        )
    }

    data class TooltipDataBundle(
        val description: Component,
        val icon: ResourceLocation
    ) : TooltipComponent
}