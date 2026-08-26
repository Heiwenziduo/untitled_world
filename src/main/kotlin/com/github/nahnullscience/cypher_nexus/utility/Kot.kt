package com.github.nahnullscience.cypher_nexus.utility

import it.unimi.dsi.fastutil.longs.AbstractLong2ReferenceMap
import it.unimi.dsi.fastutil.objects.AbstractReference2LongMap

/** no boxing */
inline fun <T> AbstractReference2LongMap<T>.forEachLong(action: (T, Long) -> Unit) {
    val iter = this.reference2LongEntrySet().iterator()
    while (iter.hasNext()) {
        val entry = iter.next()
        action(entry.key, entry.longValue)
    }
}

inline fun <T> AbstractLong2ReferenceMap<T>.forEachLong(action: (Long, T) -> Unit) {
    val iter = this.long2ReferenceEntrySet().iterator()
    while (iter.hasNext()) {
        val entry = iter.next()
        action(entry.longKey, entry.value)
    }
}
