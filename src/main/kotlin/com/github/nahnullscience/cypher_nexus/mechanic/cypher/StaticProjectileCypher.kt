package com.github.nahnullscience.cypher_nexus.mechanic.cypher

import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategories

abstract class StaticProjectileCypher(
    override val manaDrain: Float
) : AbstractProjectileCypher() {
    override val category = CypherCategories.STATIC_PROJECTILE

}