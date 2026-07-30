package com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.patterns

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.AbstractInvokingPattern
import com.github.nahnullscience.cypher_nexus.utility.CoordinateDefinition
import com.github.nahnullscience.cypher_nexus.utility.PosDirePair

object NoPattern : AbstractInvokingPattern("no_pattern") {

    override fun layout(
        index: Int,
        total: Int,
        coordinate: CoordinateDefinition,
        posDire: PosDirePair
    ): PosDirePair {
        return posDire
    }
}