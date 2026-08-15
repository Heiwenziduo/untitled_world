package com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute

interface IOperatorMap {
    fun getValue(op: AttributeOperator): Double
    fun setValue(op: AttributeOperator)
}