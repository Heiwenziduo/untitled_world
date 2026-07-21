package com.github.nahnullscience.cypher_nexus.client.gui.components

import com.github.nahnullscience.cypher_nexus.client.gui.others.GUIConstants.ELEMENT_PADDING
import com.github.nahnullscience.cypher_nexus.client.gui.others.GUIConstants.ICON_SIZE
import net.minecraft.client.gui.navigation.ScreenRectangle

/**
 * describes one fixed-size icon grid anchored at a screen origin.
 * paint and hit-test both go through this — neither hand-derives a cell position.
 *
 * @param originX left-top coordinate of the first element
 * @param originY left-top coordinate of the first element
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
        val mouseX = mouseX + mouseTolerance
        val mouseY = mouseY + mouseTolerance // weird, offset required

        val col = Math.floorDiv(mouseX - originX, elementSize)
        val borderLeft = originX + col * elementSize - mouseTolerance
        if (mouseX !in borderLeft .. borderLeft + cellSize + mouseTolerance * 2) return null

        val row = Math.floorDiv(mouseY - originY, elementSize)
        val borderTop = originY + row * elementSize - mouseTolerance
        if (mouseY !in borderTop .. borderTop + cellSize + mouseTolerance * 2) return null

        if (col !in 0 until cols || row < 0) return null
        return row * cols + col
    }
}