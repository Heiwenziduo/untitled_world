package com.github.nahnullscience.cypher_nexus.client.gui.components

import com.github.nahnullscience.cypher_nexus.client.gui.others.IndexScreenEvents.DragEnded
import com.github.nahnullscience.cypher_nexus.client.gui.others.IndexScreenEvents.DragStarted
import com.github.nahnullscience.cypher_nexus.client.gui.others.UiEventBus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import net.minecraft.client.gui.navigation.ScreenRectangle

data class DragPayload(val cypher: AbstractCypher, val sourceRect: ScreenRectangle)

/**
 * the one piece of state that legitimately crosses panel boundaries: "is something currently
 * being carried, and what." Panels never ask each other about a drag directly — they emit
 * [DragStarted]/[DragEnded] through the bus, and this class turns that into a queryable snapshot
 * any panel (or the screen) can read.
 *
 * this class does NOT render the floating icon and does NOT decide who receives mouseReleased —
 * both of those are routing/paint decisions that belong to [com.github.nahnullscience.cypher_nexus.client.gui.CypherIndexScreen],
 * for the same reason hover state lives with whoever owns the paint loop, not with the data.
 * */
class DragController(bus: UiEventBus) {
    var current: DragPayload? = null
        private set

    init {
        bus.subscribe { event ->
            when (event) {
                is DragStarted -> current = DragPayload(event.cypher, event.fromRect)
                is DragEnded -> current = null
                else -> Unit
            }
        }
    }

    val isDragging: Boolean get() = current != null
}