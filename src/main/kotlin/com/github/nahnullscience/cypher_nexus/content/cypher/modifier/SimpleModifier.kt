package com.github.nahnullscience.cypher_nexus.content.cypher.modifier

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ModifierCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttributeOperation
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import net.minecraft.core.Holder

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
    private val stateChunkHolder: HashMap<Holder<CypherAttribute>, HashMap<CypherAttributeOperation, Double>> = HashMap()

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
        operator: CypherAttributeOperation,
        value: Double
    ): SimpleModifier {
        val opMap = stateChunkHolder.getOrPut(holder) { HashMap() }
        opMap[operator] = value
        return this
    }

    fun attribute(
        holder: Holder<CypherAttribute>,
        operator: CypherAttributeOperation,
        value: Double
    ) = stateChunkAttr(holder, operator, value)

    fun modifier(): ModifierCypher = object : ModifierCypher() {
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