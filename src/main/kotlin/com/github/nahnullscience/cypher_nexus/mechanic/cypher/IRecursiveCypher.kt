package com.github.nahnullscience.cypher_nexus.mechanic.cypher

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.HelperDataBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.InvokingStateBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileStateChunk

/**
 * mark a cypher that has the ability to copy others or itself
 * */
interface IRecursiveCypher {
    companion object {
        const val RECURSION_LIMIT = 2

        fun AbstractCypher.canRecursionContinue(state: InvokingStateBundle): Boolean {
            if (this !is IRecursiveCypher) return true
            if (this.isRecursive && state.recursionDepth >= RECURSION_LIMIT) {
                CypherNexus.debugCypher { "[$this] has reached the recursion depth limit and stops function." }
                return false
            }
            return true
        }
    }

    /**
     * if the cypher may copy itself, set this to true to avoid infinite loop
     * */
    val isRecursive: Boolean

    /**
     * copy target cypher, this method will help you handle recursionDepth
     * @param targetIndex target cypher's relative index
     * */
    fun copyCypher(
        target: AbstractCypher,
        helper: InvokingHelper,
        chunk: ProjectileStateChunk,
        data: HelperDataBundle,
        state: InvokingStateBundle,
        targetIndex: Int,
    ) {
        if (target.canRecursionContinue(state)) {
            CypherNexus.debugCypher { "[$this] will copy [$target $targetIndex]" }
            if (target is IRecursiveCypher && target.isRecursive) {
                val s = state.recursionDepth++
                target.invoke(helper, chunk, data, state, targetIndex, true)
                state.recursionDepth = s
            } else
                target.invoke(helper, chunk, data, state, targetIndex, true)
        }
    }
}