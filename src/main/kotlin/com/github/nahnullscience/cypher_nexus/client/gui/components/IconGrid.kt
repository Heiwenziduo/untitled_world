package com.github.nahnullscience.cypher_nexus.client.gui.components

import com.github.nahnullscience.cypher_nexus.client.gui.others.RenderConstants.ELEMENT_PADDING
import com.github.nahnullscience.cypher_nexus.client.gui.others.RenderConstants.ICON_SIZE
import net.minecraft.client.gui.navigation.ScreenRectangle

/**
 * describes one fixed-size icon grid anchored at a screen origin.
 * paint and hit-test both go through this — neither hand-derives a cell position.
 * */
data class IconGrid(
    var originX: Int = 0,
    var originY: Int = 0,
    var cols: Int = 1,
    val cellSize: Int = ICON_SIZE,
    val cellPadding: Int = ELEMENT_PADDING,
    val mouseTolerance: Int = 1
) {
    val elementSize = cellSize + cellPadding

    fun cellRect(index: Int): ScreenRectangle {
        val col = index % cols
        val row = index / cols
        return ScreenRectangle(
            originX + col * elementSize,
            originY + row * elementSize,
            cellSize, cellSize
        )
    }

    /** null if the point falls in a gap/margin, not inside any cell */
    fun indexAt(mouseX: Int, mouseY: Int): Int? {
        val col = Math.floorDiv(mouseX - originX, elementSize)
        val row = Math.floorDiv(mouseY - originY, elementSize)
        if (col !in 0 until cols || row < 0) return null
        return row * cols + col
    }
}