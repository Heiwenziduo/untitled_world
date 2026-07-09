package com.github.nahnullscience.cypher_nexus.client.gui.components.panels

import com.github.nahnullscience.cypher_nexus.client.gui.components.IScreenRect
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.Renderable

interface IScreenPanel : IScreenRect, Renderable {

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partial: Float)
}