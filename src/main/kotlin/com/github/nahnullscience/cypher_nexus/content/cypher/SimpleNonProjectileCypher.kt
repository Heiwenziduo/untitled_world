package com.github.nahnullscience.cypher_nexus.content.cypher

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.content.cypher.modifier.SimpleModifier
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractNonProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.AttributeOperator
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.category.CypherCategory
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import net.minecraft.core.Holder
import net.minecraft.resources.Identifier
import java.util.EnumMap
import kotlin.collections.set

open class SimpleNonProjectileCypher(
    val path: String,
    val category: Holder<CypherCategory>
) : CypherDataMap.Builder() {

    var color: Int? = null
        private set

    // register timing can't unpack holder, so use holder directly here
    private val shotStateAttrHolder: HashMap<Holder<CypherAttribute>, EnumMap<AttributeOperator, Double>> = HashMap()

    override fun manaDrain(float: Float) = apply { super.manaDrain(float) }
    override fun draw(int: Int) = apply { super.draw(int) }
    override fun delay(int: Int) = apply { super.delay(int) }
    override fun recharge(int: Int) = apply { super.recharge(int) }
    override fun flags(vararg flag: CypherFlags) = apply { super.flags(*flag) }
    open fun color(int: Int) = apply { color = int }

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
        override val resource: Identifier = CypherNexus.modResource(path)
        override val color: Int? = this@SimpleNonProjectileCypher.color
        override fun defaultAttributes() = this@SimpleNonProjectileCypher
    }
}