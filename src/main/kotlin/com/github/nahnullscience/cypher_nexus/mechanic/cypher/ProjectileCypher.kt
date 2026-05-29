package com.github.nahnullscience.cypher_nexus.mechanic.cypher

import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategories

abstract class ProjectileCypher(
    override val manaDrain: Float
) : AbstractProjectileCypher() {

    override val category = CypherCategories.PROJECTILE

}