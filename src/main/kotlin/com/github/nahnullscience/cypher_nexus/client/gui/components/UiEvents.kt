package com.github.nahnullscience.cypher_nexus.client.gui.components

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import net.minecraft.client.gui.navigation.ScreenRectangle

sealed class UiEvent {
    data class CypherActivated(val cypher: AbstractCypher, val fromRect: ScreenRectangle) : UiEvent()
    data class WandSlotAssigned(val cypher: AbstractCypher, val toRect: ScreenRectangle, val fromRect: ScreenRectangle?) : UiEvent()
    data class DragStarted(val payload: AbstractCypher, val fromRect: ScreenRectangle) : UiEvent()
    data class DragEnded(val consumed: Boolean) : UiEvent()
}

class UiEventBus {
    private val listeners = mutableListOf<(UiEvent) -> Unit>()
    fun subscribe(listener: (UiEvent) -> Unit) { listeners += listener }
    fun emit(event: UiEvent) { listeners.forEach { it(event) } }
}