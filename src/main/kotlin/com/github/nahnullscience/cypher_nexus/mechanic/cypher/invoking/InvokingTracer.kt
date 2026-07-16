package com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.HelperDataBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.InvokingStateBundle

/**
 *
 * */
interface InvokingTracer {
    fun enter(
        cypher: AbstractCypher,
        chunk: ShotStateChunk,
        data: HelperDataBundle,
        state: InvokingStateBundle,
        relativeIndex: Int,
        isCopy: Boolean
    )
    fun exit(
        cypher: AbstractCypher,
        chunk: ShotStateChunk,
        data: HelperDataBundle,
        state: InvokingStateBundle
    )

    companion object {
        val NONE: InvokingTracer = object : InvokingTracer {
            override fun enter(
                cypher: AbstractCypher,
                chunk: ShotStateChunk,
                data: HelperDataBundle,
                state: InvokingStateBundle,
                relativeIndex: Int,
                isCopy: Boolean
            ) = Unit
            override fun exit(
                cypher: AbstractCypher,
                chunk: ShotStateChunk,
                data: HelperDataBundle,
                state: InvokingStateBundle
            ) = Unit
        }
    }
}