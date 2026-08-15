package com.github.nahnullscience.cypher_nexus.content.cypher.modifier

import com.github.nahnullscience.cypher_nexus.content.cypher.SimpleNonProjectileCypher
import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategories
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ModifierCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.AttributeOperator
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import com.github.nahnullscience.cypher_nexus.utility.toRGB
import net.minecraft.core.Holder
import net.minecraft.resources.Identifier
import java.awt.Color

/** easy way to create lots of simple modifiers */
class SimpleModifier(
    path: Identifier,
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
    override fun borderColor(color: Int) = apply { super.borderColor(color) }
    override fun dyeColor(rgb: Int) = apply { super.dyeColor(rgb) }
    override fun dyeColor(color: Color) = apply { super.dyeColor(color) }
    override fun dyeColor(rgb: Int, a: Float) = apply { super.dyeColor(rgb, a) }
    override fun dyeColor(alpha: Float) = apply { super.dyeColor(alpha) }
    override fun brightness(l: Float) = apply { super.brightness(l) }

    override fun shotStateAttr(
        holder: Holder<CypherAttribute>,
        operator: AttributeOperator,
        value: Double
    ): SimpleModifier = apply { super.shotStateAttr(holder, operator, value) }

    override fun createCypher(): ModifierCypher = object : ModifierCypher(UNMODIFIED) {
        override val resource: Identifier = this@SimpleModifier.path
        override val overrideBorder: Boolean = this@SimpleModifier.border
        override val borderColor: Int = this@SimpleModifier.borderColor
        override val rgb: Color? = this@SimpleModifier.rgb?.toRGB()
        override val alpha: Float = this@SimpleModifier.alpha
        override val brightness: Float = this@SimpleModifier.brightness
        override fun defaultAttributes() = this@SimpleModifier
    }

    fun createModifier(): ModifierCypher = createCypher()
}