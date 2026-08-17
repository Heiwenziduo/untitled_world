package com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.patterns

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.AbstractInvokingPattern
import com.github.nahnullscience.cypher_nexus.utility.linear_space.AnchoredCoordinate
import com.github.nahnullscience.cypher_nexus.utility.linear_space.PosDirePair

object NoPattern : AbstractInvokingPattern("no_pattern") {

    override fun layout(
        index: Int,
        total: Int,
        coordinate: AnchoredCoordinate
    ): PosDirePair {
        return posDire
    }
}