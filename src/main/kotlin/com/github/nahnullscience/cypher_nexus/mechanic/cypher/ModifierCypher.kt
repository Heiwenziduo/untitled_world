package com.github.nahnullscience.cypher_nexus.mechanic.cypher

import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategoryRegistry
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.category.CypherCategory
import net.minecraft.core.Holder

abstract class ModifierCypher (
    override val manaDrain: Float
) : AbstractNonProjectileCypher() {
    init {

    }
    override val draw: Int = 1
    override val category: Holder<CypherCategory> = CypherCategoryRegistry.MODIFIER
}