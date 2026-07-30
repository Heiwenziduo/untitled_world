package com.github.nahnullscience.cypher_nexus.content.cypher

import com.github.nahnullscience.cypher_nexus.init.mod.InvokingPatterns.NO_PATTERN
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractNonProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.AttributeOperator
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.category.CypherCategory
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.AbstractInvokingPattern
import com.github.nahnullscience.cypher_nexus.utility.toRGB
import net.minecraft.core.Holder
import net.minecraft.resources.Identifier
import java.awt.Color
import java.util.*

open class SimpleNonProjectileCypher(
    val path: Identifier,
    val category: Holder<CypherCategory>
) : CypherDataMap.Builder() {

    protected var borderColor: Int? = null
    protected var rgb: Int? = null
    protected var alpha: Float? = null
    protected var brightness: Float? = null
    protected var pattern: Holder<AbstractInvokingPattern> = NO_PATTERN

    // register timing can't unpack holder, so use holder directly here
    private val shotStateAttrHolder: HashMap<Holder<CypherAttribute>, EnumMap<AttributeOperator, Double>> = HashMap()

    override fun manaDrain(float: Float) = apply { super.manaDrain(float) }
    override fun draw(int: Int) = apply { super.draw(int) }
    override fun delay(int: Int) = apply { super.delay(int) }
    override fun recharge(int: Int) = apply { super.recharge(int) }
    override fun flags(vararg flag: CypherFlags) = apply { super.flags(*flag) }
    open fun borderColor(color: Int) = apply { borderColor = color }
    open fun dyeColor(rgb: Int) = apply { this.rgb = rgb }
    open fun dyeColor(color: Color) = apply {
        this.rgb = color.rgb
        if (color.alpha != 0xff) alpha = color.alpha.toFloat() / 255
    }
    open fun dyeColor(rgb: Int, a: Float) = apply { this.rgb = rgb; alpha = a }
    open fun dyeColor(alpha: Float) = apply { this.alpha = alpha }
    open fun brightness(l: Float) = apply { brightness = l }
    open fun pattern(p: Holder<AbstractInvokingPattern>) = apply { pattern = p }

    // do nothing since this is non-projectile
    override fun projectileAttr(holder: Holder<CypherAttribute>, value: Double) = this as CypherDataMap.Builder
    override fun shotStateAttr(holder: Holder<CypherAttribute>, operator: AttributeOperator, value: Double) = apply {
        val opMap = shotStateAttrHolder.getOrPut(holder) { EnumMap(AttributeOperator::class.java) }
        opMap[operator] = value
    }

    override fun build(): CypherDataMap {
        // this timing should be fine
        shotStateAttrHolder.forEach { (holder, opMap) ->
            opMap.forEach { (op, d) ->
                super.shotStateAttr(holder, op, d)
            }
        }
        return super.build()
    }

    open fun createCypher() : AbstractNonProjectileCypher = object : AbstractNonProjectileCypher() {
        override val category = this@SimpleNonProjectileCypher.category
        override val resource: Identifier = this@SimpleNonProjectileCypher.path
        override val borderColor: Int? = this@SimpleNonProjectileCypher.borderColor
        override val rgb: Color? = this@SimpleNonProjectileCypher.rgb?.toRGB()
        override val alpha: Float? = this@SimpleNonProjectileCypher.alpha
        override val brightness: Float? = this@SimpleNonProjectileCypher.brightness
        override val pattern: Holder<AbstractInvokingPattern> = this@SimpleNonProjectileCypher.pattern
        override fun defaultAttributes() = this@SimpleNonProjectileCypher
    }
}