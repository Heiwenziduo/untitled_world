package com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.HelperDataBundle

/**
 *
 * */
interface InvokingTracer {
    fun enter(
        cypher: AbstractCypher,
        shotState: ShotState,
        data: HelperDataBundle,
        paras: InvokingSharedParameter,
        relativeIndex: Int,
        isCopy: Boolean
    )
    fun modify(
        helper: InvokingHelper,
        shotState: ShotState,
        data: HelperDataBundle,
        paras: InvokingSharedParameter,
        isCopy: Boolean
    )
    fun exit(
        cypher: AbstractCypher,
        shotState: ShotState,
        data: HelperDataBundle,
        paras: InvokingSharedParameter
    )

    companion object {
        val NONE: InvokingTracer = object : InvokingTracer {
            override fun enter(
                cypher: AbstractCypher,
                shotState: ShotState,
                data: HelperDataBundle,
                paras: InvokingSharedParameter,
                relativeIndex: Int,
                isCopy: Boolean
            ) = Unit

            override fun modify(
                helper: InvokingHelper,
                shotState: ShotState,
                data: HelperDataBundle,
                paras: InvokingSharedParameter,
                isCopy: Boolean
            ) = Unit

            override fun exit(
                cypher: AbstractCypher,
                shotState: ShotState,
                data: HelperDataBundle,
                paras: InvokingSharedParameter
            ) = Unit
        }
    }
}