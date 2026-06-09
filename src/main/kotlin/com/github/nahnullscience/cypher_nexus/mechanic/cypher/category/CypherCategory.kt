package com.github.nahnullscience.cypher_nexus.mechanic.cypher.category

import com.github.nahnullscience.cypher_nexus.utility.i.IRegisterable
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.Identifier

class CypherCategory(
    override val resource: Identifier,
    val color: Int
): IRegisterable {
    /** lang-JSON key: cypher.category.{MOD_ID}.{category_name} */
    override fun translation(): MutableComponent =
        Component.translatable("cypher.category.${resource.namespace}.${resource.path}")
}