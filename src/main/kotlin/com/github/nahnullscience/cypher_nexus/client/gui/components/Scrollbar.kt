package com.github.nahnullscience.cypher_nexus.client.gui.components

import com.github.nahnullscience.cypher_nexus.client.gui.others.GUIConstants.SCROLLBAR_WIDTH
import com.github.nahnullscience.cypher_nexus.client.gui.others.Classic
import com.github.nahnullscience.cypher_nexus.client.gui.others.ColorTheme
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.util.Mth
import kotlin.math.max

/**
 * a self-contained vertical scrollbar. it owns no screen region of its own —
 * it borrows a thin strip along the right edge of whatever [IScreenRect] it's told to [layout] against.
 * it doesn't know what it's scrolling; the owner reads [offset] each frame and applies it manually
 * when laying out its own content. not a Panel, not on the event bus — purely local helper state.
 * */
class Scrollbar(val theme: ColorTheme = Classic) {

    var offset: Double = 0.0
        private set

    private var width: Int = SCROLLBAR_WIDTH
    private var viewportHeight: Int = 0
    private var contentHeight: Int = 0

    private var trackX = 0
    private var trackY = 0
    private var trackH = 0

    private var dragging = false

    val maxOffset: Double get() = max(0.0, (contentHeight - viewportHeight).toDouble())
    val isNeeded: Boolean get() = maxOffset > 0.0

    private val thumbHeight: Int
        get() = if (!isNeeded) trackH
        else max(20, (viewportHeight.toDouble() / contentHeight * trackH).toInt())

    private val thumbY: Int
        get() = if (maxOffset <= 0.0) trackY
        else trackY + ((offset / maxOffset) * (trackH - thumbHeight)).toInt()

    /**
     * automatically attach the scrollbar to the right side of the given [IScreenRect].
     *
     * call every time the owner's bounds or content size change (resize, filter, data reload).
     * cheap — a handful of field writes, no iteration over icons.
     * */
    fun layout(bounds: IScreenRect, viewportHeight: Int, contentHeight: Int) {
        this.viewportHeight = viewportHeight
        this.contentHeight = contentHeight
        trackX = bounds.right - width - 2
        trackY = bounds.top
        trackH = bounds.h
        offset = offset.coerceIn(0.0, maxOffset) // clamp in case content shrank (e.g. a filter applied)
    }

    fun layout(rect: ScreenRectangle, viewportHeight: Int, contentHeight: Int) {
        this.viewportHeight = viewportHeight
        this.contentHeight = contentHeight
        trackX = rect.left()
        trackY = rect.top()
        trackH = rect.height
        width = rect.width
        offset = offset.coerceIn(0.0, maxOffset)
    }

    fun scrollBy(lines: Double, lineHeight: Int) {
        offset = (offset - lines * lineHeight).coerceIn(0.0, maxOffset)
    }

    fun contains(mouseX: Double, mouseY: Double): Boolean =
        isNeeded && mouseX in trackX.toDouble()..(trackX + width).toDouble()
                && mouseY in trackY.toDouble()..(trackY + trackH).toDouble()

    /** @return true if this click landed on the bar and should be consumed */
    fun mouseClicked(mouseX: Double, mouseY: Double): Boolean {
        if (!contains(mouseX, mouseY)) return false
        dragging = true
        jumpTo(mouseY)
        return true
    }

    /** @param mouseY absolute mouse Y from the event — NOT a delta */
    fun mouseDragged(mouseY: Double): Boolean {
        if (!dragging) return false
        jumpTo(mouseY)
        return true
    }

    /** @return true if a drag was in progress and is now released */
    fun mouseReleased(): Boolean {
        if (!dragging) return false
        dragging = false
        return true
    }

    private fun jumpTo(mouseY: Double) {
        if (maxOffset <= 0.0) return
        val usable = (trackH - thumbHeight).coerceAtLeast(1)
        val pct = Mth.clamp((mouseY - trackY - thumbHeight / 2.0) / usable, 0.0, 1.0)
        offset = pct * maxOffset
    }

    fun render(graphics: GuiGraphicsExtractor) {
        if (!isNeeded) return
        graphics.fill(trackX, trackY, trackX + width, trackY + trackH, theme.secondary)
        graphics.fill(trackX, thumbY, trackX + width, thumbY + thumbHeight, theme.primary)
    }
}