package com.github.nahnullscience.cypher_nexus.content.cypher.modifier

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.content.cypher.SimpleNonProjectileCypher
import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategories
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ModifierCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.AttributeOperator
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import net.minecraft.core.Holder
import net.minecraft.resources.Identifier

/** easy way to create lots of simple modifiers */
class SimpleModifier(
    path: String,
    manaDrain: Float,
) : SimpleNonProjectileCypher(path, CypherCategories.MODIFIER) {
    init {
        manaDrain(manaDrain)
        draw(1)
    }

    override fun manaDrain(float: Float) = apply { super.manaDrain(float) }
    override fun draw(int: Int) = apply { super.draw(int) }
    override fun delay(int: Int) = apply { super.delay(int) }
    override fun recharge(int: Int) = apply { super.recharge(int) }
    override fun flags(vararg flag: CypherFlags) = apply { super.flags(*flag) }
    override fun color(int: Int) = apply { super.color(int) }

    override fun shotStateAttr(
        holder: Holder<CypherAttribute>,
        operator: AttributeOperator,
        value: Double
    ): SimpleModifier = apply { super.shotStateAttr(holder, operator, value) }

    override fun createCypher(): ModifierCypher = object : ModifierCypher(NONE_ATTR) {
        override val resource: Identifier = CypherNexus.modResource(path)
        override val color: Int? = this@SimpleModifier.color
        override fun defaultAttributes() = this@SimpleModifier
    }

    fun createModifier(): ModifierCypher = createCypher()
}