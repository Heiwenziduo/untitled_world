package com.github.nahnullscience.cypher_nexus.mechanic.cypher

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.HelperDataBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingSharedParameter
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotState
import org.apache.logging.log4j.Level

/**
 * mark a cypher that has the ability to copy others or itself
 * */
interface IRecursiveCypher {
    companion object {
        const val RECURSION_LIMIT = 2

        fun AbstractCypher.canRecursionContinue(paras: InvokingSharedParameter): Boolean {
            if (this !is IRecursiveCypher) return true
            if (this.isRecursive && paras.recursionDepth >= RECURSION_LIMIT) {
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
        shotState: ShotState,
        data: HelperDataBundle,
        paras: InvokingSharedParameter,
        targetIndex: Int,
    ) {
        if (target.canRecursionContinue(paras)) {
            CypherNexus.debugCypher { "[$this] will copy [$target $targetIndex]" }
            if (target is IRecursiveCypher && target.isRecursive) {
                val s = paras.recursionDepth++
                target.traceInvoke(helper, shotState, data, paras, targetIndex, true)
                paras.recursionDepth = s
            } else
                target.traceInvoke(helper, shotState, data, paras, targetIndex, true)
        }
    }

    /**
     * copy the cypher at given [index], call [copyCypher] internally
     * */
    fun copyCypherIndexed(
        index: Int,
        helper: InvokingHelper,
        shotState: ShotState,
        data: HelperDataBundle,
        paras: InvokingSharedParameter,
        targetIndex: Int,
    ) {
        val cy = helper.aoc.getInvokableOrNull(index)
        if (cy == null) {
            CypherNexus.debugCypher(Level.WARN)
            { "get uninvokable cypher on [index $index], copy failed." }
            return
        }
        copyCypher(cy, helper, shotState, data, paras, targetIndex)
    }
}