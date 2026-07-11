package com.github.nahnullscience.cypher_nexus.client.gui.others

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import net.minecraft.client.gui.navigation.ScreenRectangle

sealed class IndexScreenEvents {

    /**
     * emits when a cypher in library panel being right-clicked
     * */
    data class CypherQuickAssign(val cypher: AbstractCypher, val fromRect: ScreenRectangle) : IndexScreenEvents()

    /**
     *
     * */
    data class WandSlotAssigned(val cypher: AbstractCypher, val toRect: ScreenRectangle, val fromRect: ScreenRectangle?) : IndexScreenEvents()

    /**
     *
     * */
    data class DragStarted(val payload: AbstractCypher, val fromRect: ScreenRectangle) : IndexScreenEvents()

    /**
     *
     * */
    data class DragEnded(val consumed: Boolean) : IndexScreenEvents()
}

class UiEventBus {
    private val listeners = mutableListOf<(IndexScreenEvents) -> Unit>()
    fun subscribe(listener: (IndexScreenEvents) -> Unit) { listeners += listener }
    fun emit(event: IndexScreenEvents) { listeners.forEach { it(event) } }
}