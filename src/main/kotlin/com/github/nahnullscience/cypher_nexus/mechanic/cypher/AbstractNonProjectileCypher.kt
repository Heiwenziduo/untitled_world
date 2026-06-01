package com.github.nahnullscience.cypher_nexus.mechanic.cypher

abstract class AbstractNonProjectileCypher: AbstractCypher() {

    final override fun triggerCanAttach() = false
    final override fun triggerCanPayload() = false

}