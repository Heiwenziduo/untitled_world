package com.github.nahnullscience.cypher_nexus.mechanic

class CypherNotFoundException(override val message: String?) : Exception(message) {
}