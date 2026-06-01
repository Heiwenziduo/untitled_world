package com.github.nahnullscience.cypher_nexus.mechanic.cypher

import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategories
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.category.CypherCategory
import net.minecraft.core.Holder

abstract class ModifierCypher () : AbstractNonProjectileCypher() {

    override val category: Holder<CypherCategory> = CypherCategories.MODIFIER
    override fun defaultAttributes() = super.defaultAttributes().draw(1)
}