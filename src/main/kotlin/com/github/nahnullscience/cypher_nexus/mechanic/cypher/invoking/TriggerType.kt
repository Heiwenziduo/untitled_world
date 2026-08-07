package com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking

import java.util.Locale.getDefault

/**
 *
 * */
enum class TriggerType(
    val timer: Int,
    val loop: Boolean = false,
) {
    NONE(-1),
    COLLISION(-1),
    TIMER_10(10),
    TIMER_20(20),
    TIMER_30(30),
    TIMER_40(40),
    TIMER_70(70),
    TIMER_200(200),
    DEATH(-1),
    RED_STONE(-1), // TODO

    ;
    val simpleName: String get() = name.lowercase(getDefault())
    val isTimer: Boolean get() = timer > 0
}