package com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.HelperDataBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.InvokingParameterBundle

/**
 *
 * */
interface InvokingTracer {
    fun enter(
        cypher: AbstractCypher,
        shotState: ShotStateChunk,
        data: HelperDataBundle,
        paras: InvokingParameterBundle,
        relativeIndex: Int,
        isCopy: Boolean
    )
    fun exit(
        cypher: AbstractCypher,
        shotState: ShotStateChunk,
        data: HelperDataBundle,
        paras: InvokingParameterBundle
    )

    companion object {
        val NONE: InvokingTracer = object : InvokingTracer {
            override fun enter(
                cypher: AbstractCypher,
                shotState: ShotStateChunk,
                data: HelperDataBundle,
                paras: InvokingParameterBundle,
                relativeIndex: Int,
                isCopy: Boolean
            ) = Unit
            override fun exit(
                cypher: AbstractCypher,
                shotState: ShotStateChunk,
                data: HelperDataBundle,
                paras: InvokingParameterBundle
            ) = Unit
        }
    }
}