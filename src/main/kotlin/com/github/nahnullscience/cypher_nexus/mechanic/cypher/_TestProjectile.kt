package com.github.nahnullscience.cypher_nexus.mechanic.cypher

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategoryRegistry

object _TestProjectile: ProjectileCypher(
    manaDrain = 5f
) {
    override val category = CypherCategoryRegistry.OTHER
    override val resource = CypherNexus.modResource("test_projectile")
    init {
        addAttribute(CypherAttributes.DAMAGE, 1.0)
        addAttribute(CypherAttributes.SPEED, 0.5)
        addAttribute(CypherAttributes.EXISTING, 200.0)
        addAttribute(CypherAttributes.BOUNCE, 5.0)

    }
}