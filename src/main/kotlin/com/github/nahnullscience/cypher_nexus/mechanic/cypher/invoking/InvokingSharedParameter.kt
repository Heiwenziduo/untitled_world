package com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap

/**
 * for special data persist along the invoking
 * */
data class InvokingSharedParameter (
    var wrapped: Boolean = false,
    var drawEnabled: Boolean = true,
    var recursionDepth: Int = 0,

    var alreadyRefreshed: Boolean = false,
    var diByChainDepthCurrent: Int = 0,
    var diByChainDepthMax: Int = 0,
) {
    fun disableDraw() = run { drawEnabled = false }
    fun enableDraw() = run { drawEnabled = true }


    // extensive refactor
    abstract class InvokingParameterTicket <T> {}

    private var _mapBacking: Reference2ReferenceOpenHashMap<InvokingParameterTicket<*>, Slot<*>>? = null
    private val map
        get() = _mapBacking ?: Reference2ReferenceOpenHashMap<InvokingParameterTicket<*>, Slot<*>>(8)
            .also { _mapBacking = it }.also { it.defaultReturnValue(null) }

    @Suppress("UNCHECKED_CAST")
    operator fun <T : Any> get(ticket: InvokingParameterTicket<T>): T? = _mapBacking?.let { (map[ticket] as Slot<T>?)?.value } // won't create map through get

    operator fun <T : Any> set(ticket: InvokingParameterTicket<T>, value: T): T? {
        val old = this[ticket]
        map[ticket] = Slot(ticket, value)
        return old
    }

    inline fun <T : Any> getOrPut(ticket: InvokingParameterTicket<T>, supplier: () -> T): T {
        return this[ticket] ?: supplier().also { this[ticket] = it }
    }

    private class Slot <T : Any> (val ticket: InvokingParameterTicket<T>, var value: T)
}