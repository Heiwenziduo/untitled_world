package com.github.nahnullscience.cypher_nexus.content.cypher.modifier

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ModifierCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.AttributeOperator
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import net.minecraft.core.Holder
import java.util.EnumMap

/** easy way to create lots of simple modifiers */
class SimpleModifier(
    val path: String,
    manaDrain: Float,
) : CypherDataMap.Builder() {
    init {
        manaDrain(manaDrain)
        draw(1)
    }

    private var _color: Int = 0
    // register timing can't unpack holder, so use holder directly here
    private val stateChunkHolder: HashMap<Holder<CypherAttribute>, EnumMap<AttributeOperator, Double>> = HashMap()

    override fun manaDrain(float: Float) = run { super.manaDrain(float); this }
    override fun draw(int: Int) = run { super.draw(int); this }
    override fun delay(int: Int) = run { super.delay(int); this }
    override fun recharge(int: Int) = run { super.recharge(int); this }
    override fun flags(vararg flag: CypherFlags) = run { super.flags(*flag); this }
    fun color(int: Int) = run { _color = int; this }

    // do nothing since this is a modifier
    override fun projectileAttr(holder: Holder<CypherAttribute>, value: Double) = this as CypherDataMap.Builder

    override fun stateChunkAttr(
        holder: Holder<CypherAttribute>,
        operator: AttributeOperator,
        value: Double
    ): SimpleModifier {
        val opMap = stateChunkHolder.getOrPut(holder) { EnumMap(AttributeOperator::class.java) }
        opMap[operator] = value
        return this
    }

    fun createModifier(): ModifierCypher = object : ModifierCypher(NONE) {
        override val resource = CypherNexus.modResource(path)
        override val color = _color
        override fun defaultAttributes() = this@SimpleModifier
    }

    override fun build(): CypherDataMap {
        // this timing should be fine
        stateChunkHolder.forEach { (holder, opMap) ->
            opMap.forEach { (op, d) ->
                super.stateChunkAttr(holder, op, d)
            }
        }
        return super.build()
    }
}