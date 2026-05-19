package com.github.nahnullscience.cypher_nexus.utility

object MathUtility {
    /**  */
    fun toSameSymbol(number: Double, sample: Double): Double {
        if (number == 0.0 || sample == 0.0) return number
        return if (number * sample > 0) number else number * -1
    }
}