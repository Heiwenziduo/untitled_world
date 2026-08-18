package com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking

import com.github.nahnullscience.cypher_nexus.utility.mod.MapOfCypherCounts

object ShotStatePool {
    // TODO pool

    fun getOrCreateShotState(ccMap: MapOfCypherCounts) : ShotState {
        return ShotState(ccMap).compute()
    }
}