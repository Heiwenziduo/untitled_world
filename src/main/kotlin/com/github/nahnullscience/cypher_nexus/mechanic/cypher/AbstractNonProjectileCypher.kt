package com.github.nahnullscience.cypher_nexus.mechanic.cypher

import java.awt.Color

abstract class AbstractNonProjectileCypher(
    defaultAttribute: CypherDataMap.Builder.() -> CypherDataMap.Builder = NONE_ATTR
): AbstractCypher(defaultAttribute) {

    open val rgb: Color? = null
    open val alpha: Float = Float.NaN
    open val brightness: Float = Float.NaN

    override fun triggerInterplay() = false
}