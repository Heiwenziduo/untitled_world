package com.github.nahnullscience.cypher_nexus.mechanic.cypher

interface ISecondaryInvokeMarker {
    val subDraw: Int
    val ignoreMana: Boolean
        get() = false
}