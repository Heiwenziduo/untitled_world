package com.github.nahnullscience.cypher_nexus.utility.mod

import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategoryRegistry
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttributeOperation
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.category.CypherCategory
import kotlin.collections.forEach

object CypherUtility {
    fun attributeCalculator(opMap: HashMap<CypherAttributeOperation, Double>, base: Double) : Double {
        val a = opMap.getOrDefault(CypherAttributeOperation.ADD, CypherAttributeOperation.ADD.defaultValue)
        val m1 = opMap.getOrDefault(CypherAttributeOperation.MULTIPLY_BASE, CypherAttributeOperation.MULTIPLY_BASE.defaultValue)
        val m2 = opMap.getOrDefault(CypherAttributeOperation.MULTIPLY_TOTAL, CypherAttributeOperation.MULTIPLY_TOTAL.defaultValue)
        val s = opMap[CypherAttributeOperation.SET]
        return s ?: ((base + a) * (m1 + 1) * m2)
    }

    fun sortCyphersByCategory(list: List<AbstractCypher>): Map<CypherCategory, List<AbstractCypher>> {
        val map = mutableMapOf<CypherCategory, MutableList<AbstractCypher>>()
        CypherCategoryRegistry.REGISTRY.toList().forEach { category -> map.put(category, mutableListOf()) } // this will keep map in category registry order
        list.forEach { cypher ->
            val list0 = map.getValue(cypher.category.value())
            list0.add(cypher)
        }
        return map
    }


//    fun <T> com(a: T, b: T): T
//    where T : Number, T : Comparable<T> {
//        return a + b
//    }
}