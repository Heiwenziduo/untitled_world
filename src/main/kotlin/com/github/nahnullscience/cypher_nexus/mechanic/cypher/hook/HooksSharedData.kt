package com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity.Companion.GENERIC_CAPTURE_RADIUS_SQR
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap
import net.minecraft.world.entity.Entity

class HooksSharedData {

    abstract class DataTicket <T> {
        /**
         * called every tick to verify data availability.
         * @return true if outdated, then [data] will be cleared from map
         * */
        abstract fun <CE> shouldAbortData(cyEntity: CE, data: T): Boolean where CE : Entity, CE : ICypherEntity
    }

    companion object {
        val HomingTicket = object : DataTicket<Entity>() {
            override fun <CE> shouldAbortData(
                cyEntity: CE,
                data: Entity
            ): Boolean where CE : Entity, CE : ICypherEntity {
                // clear target if too far
                return data.isRemoved || data.distanceToSqr(cyEntity.position()) > GENERIC_CAPTURE_RADIUS_SQR * 2
            }
        }
    }

    private var _mapBacking: Reference2ReferenceOpenHashMap<DataTicket<*>, Slot<*>>? = null
    private val map get() = _mapBacking ?: Reference2ReferenceOpenHashMap<DataTicket<*>, Slot<*>>(4)
        .also { _mapBacking = it }.also { it.defaultReturnValue(null) }

    var homingTarget: Entity?
        get() = this[HomingTicket]
        set(value) {
            value?.let { this[HomingTicket] = it }
        }

    fun <CE> tick(ce: CE) where CE : Entity, CE : ICypherEntity {
        _mapBacking?.values?.iterator()?.let {
            while (it.hasNext()) {
                if (it.next().shouldAbort(ce)) it.remove()
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <T : Any> get(ticket: DataTicket<T>): T? = _mapBacking?.let { (map[ticket] as Slot<T>?)?.value } // won't create map through get

    operator fun <T : Any> set(ticket: DataTicket<T>, value: T): T? {
        val old = this[ticket]
        map[ticket] = Slot(ticket, value)
        return old
    }

    inline fun <T : Any> getOrPut(ticket: DataTicket<T>, supplier: () -> T): T {
        return this[ticket] ?: supplier().also { this[ticket] = it }
    }

    private class Slot<T : Any>(val ticket: DataTicket<T>, var value: T) {
        // wrap again to provide type safety
        fun <CE> shouldAbort(ce: CE): Boolean where CE : Entity, CE : ICypherEntity = ticket.shouldAbortData(ce, value)
    }
}