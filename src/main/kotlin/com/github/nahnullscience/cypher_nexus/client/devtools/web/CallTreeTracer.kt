package com.github.nahnullscience.cypher_nexus.client.devtools.web

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.HelperDataBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.InvokingParameterBundle
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
        shotState: ShotStateChunk,
        data: HelperDataBundle,
        paras: InvokingParameterBundle,
        relativeIndex: Int,
        isCopy: Boolean
    ) {
        val node = Node(cypher, isCopy)
        (stack.lastOrNull()?.children ?: roots).add(node)
        stack.addLast(node)
    }

    override fun modify(
        helper: InvokingHelper,
        shotState: ShotStateChunk,
        data: HelperDataBundle,
        paras: InvokingParameterBundle,
        isCopy: Boolean
    ) {

    }

    override fun exit(
        cypher: AbstractCypher,
        shotState: ShotStateChunk,
        data: HelperDataBundle,
        paras: InvokingParameterBundle
    ) {
        stack.removeLast()
    }
}
