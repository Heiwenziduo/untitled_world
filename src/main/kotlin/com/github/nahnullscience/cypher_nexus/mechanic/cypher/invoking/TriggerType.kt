package com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking

import java.util.Locale.getDefault

/**
 *
 * */
enum class TriggerType(
    val timer: Int?,
    val loop: Boolean = false,
) {
    NONE(null),
    COLLISION(null),
    TIMER_10(10),
    TIMER_20(20),
    TIMER_30(30),
    TIMER_40(40),
    TIMER_70(70),
    TIMER_200(200),
    DEATH(null),
    RED_STONE(null), // TODO

    ;
    val simpleName: String get() = name.lowercase(getDefault())
    val isTimer: Boolean get() = timer != null && timer > 0
}