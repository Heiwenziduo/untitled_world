package com.github.nahnullscience.cypher_nexus.mechanic.cypher.compiler

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher

class CompilationContext(
    val helperData: InvokerHelper.HelperDataBundle,
    val cypherList: List<AbstractCypher>
) {
    var pendingDynamicPayload: InvokerHelper.CypherPayloadBlock? = null

    // Wrap tracking
    var hasWrapped = false
    var totalCyphersReadThisInvoke = 0

    fun getNextCypher(): AbstractCypher? {
        // Rule 2: Prevent infinite loops (don't read more cyphers than exist in the wand)
        if (totalCyphersReadThisInvoke >= cypherList.size) return null

        // Rule 1: Handle wrapping
        if (helperData.index >= cypherList.size) {
            if (hasWrapped) return null // Already wrapped once this cast

            hasWrapped = true
            helperData.index = 0 // Loop back to the discard pile
        }

        totalCyphersReadThisInvoke++
        return cypherList[helperData.index++]
    }
}