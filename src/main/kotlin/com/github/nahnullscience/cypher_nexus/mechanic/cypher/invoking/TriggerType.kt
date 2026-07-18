package com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking

import java.util.Locale.getDefault

/**
 *
 * */
enum class TriggerType() {
    NONE,
    COLLISION,
    TIMER_5,
    TIMER_10,
    TIMER_20,
    TIMER_40,
    TIMER_70,
    TIMER_200,
    DEATH,
    RED_STONE, // TODO

    ;
    var simpleName: String = ""
        private set

    init {
        simpleName = name.lowercase(getDefault())
    }
}