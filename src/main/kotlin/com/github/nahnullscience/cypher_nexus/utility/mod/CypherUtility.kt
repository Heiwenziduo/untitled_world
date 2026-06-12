package com.github.nahnullscience.cypher_nexus.utility.mod

import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategories
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.AttributeOperator
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.category.CypherCategory
import kotlin.collections.forEach

object CypherUtility {

    fun sortCyphersByCategory(list: List<AbstractCypher>): Map<CypherCategory, List<AbstractCypher>> {
        val map = mutableMapOf<CypherCategory, MutableList<AbstractCypher>>()
        CypherCategories.REGISTRY.toList().forEach { category -> map[category] = mutableListOf() } // this will keep map in category registry order
        list.forEach { cypher ->
            val list0 = map.getValue(cypher.category.value())
            list0.add(cypher)
        }
        return map
    }

}