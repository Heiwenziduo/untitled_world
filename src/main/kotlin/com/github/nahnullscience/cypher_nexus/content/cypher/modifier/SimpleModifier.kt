package com.github.nahnullscience.cypher_nexus.content.cypher.modifier

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ModifierCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttributeOperation
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import net.minecraft.core.Holder

/** easy way to create lots of simple modifiers */
class SimpleModifier(
    manaDrain: Float,
    path: String,
    override val delay: Int = 0,
    override val recharge: Int = 0,
    override val draw: Int = 1,
    override val color: Int = 0
) : ModifierCypher(manaDrain) {
    override val resource = CypherNexus.modResource(path)
    fun attribute(attribute: Holder<CypherAttribute>, operator: CypherAttributeOperation, value: Double) = run {
        addAttribute(attribute, operator, value)
        this@SimpleModifier
    }

    fun flag(flag: CypherFlags) = run {
        addFlag(flag)
        this@SimpleModifier
    }
}