package com.github.nahnullscience.cypher_nexus.mechanic.cypher

abstract class AbstractNonProjectileCypher(
    defaultAttribute: CypherDataMap.Builder.() -> CypherDataMap.Builder = NONE_ATTR
): AbstractCypher(defaultAttribute) {

    override fun triggerInterplay() = false

}