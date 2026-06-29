package com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking

import com.github.nahnullscience.cypher_nexus.utility.mod.MapOfCypherCounts

object StateChunkPool {
    // TODO pool

    fun getOrCreateStateChunk(mocc: MapOfCypherCounts) : ShotStateChunk {
        return ShotStateChunk(mocc).compute()
    }
}