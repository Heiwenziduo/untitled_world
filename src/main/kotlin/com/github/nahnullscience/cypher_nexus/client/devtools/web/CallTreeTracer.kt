package com.github.nahnullscience.cypher_nexus.client.devtools.web

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.HelperDataBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.InvokingStateBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingTracer
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk

class CallTreeTracer : InvokingTracer {
    class Node(val cypher: AbstractCypher, val isCopy: Boolean) {
        val children = mutableListOf<Node>()
    }
    val roots = mutableListOf<Node>()
    private val stack = ArrayDeque<Node>()

    override fun enter(
        cypher: AbstractCypher,
        chunk: ShotStateChunk,
        data: HelperDataBundle,
        state: InvokingStateBundle,
        relativeIndex: Int,
        isCopy: Boolean
    ) {
        val node = Node(cypher, isCopy)
        (stack.lastOrNull()?.children ?: roots).add(node)
        stack.addLast(node)
    }
    override fun exit(
        cypher: AbstractCypher,
        chunk: ShotStateChunk,
        data: HelperDataBundle,
        state: InvokingStateBundle
    ) {
        stack.removeLast()
    }
}
