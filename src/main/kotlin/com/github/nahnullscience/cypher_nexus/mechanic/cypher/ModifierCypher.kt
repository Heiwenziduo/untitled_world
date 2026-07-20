package com.github.nahnullscience.cypher_nexus.mechanic.cypher

import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategories
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.category.CypherCategory
import net.minecraft.core.Holder

abstract class ModifierCypher (
    defaultAttribute: CypherDataMap.Builder.() -> CypherDataMap.Builder
) : AbstractNonProjectileCypher(defaultAttribute) {

    final override val category: Holder<CypherCategory> = CypherCategories.MODIFIER

    override fun defaultAttributes() = CypherDataMap.builder().draw(1).defaultAttribute()
}