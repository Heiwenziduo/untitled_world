package com.github.nahnullscience.cypher_nexus.client.gui.panels

import com.github.nahnullscience.cypher_nexus.client.gui.components.IScreenRect
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.Renderable
import net.minecraft.client.input.MouseButtonEvent

interface IScreenPanel : IScreenRect, Renderable {

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partial: Float)

    fun contains(mouseX: Double, mouseY: Double): Boolean =
        mouseX >= left && mouseX < right && mouseY >= top && mouseY < bot

    // default no-op: a panel that doesn't care about input simply doesn't override these
    fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean = false
    fun mouseDragged(event: MouseButtonEvent, dx: Double, dy: Double): Boolean = false
    fun mouseReleased(event: MouseButtonEvent): Boolean = false
    fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean = false
    fun onScreenClose() {}
}