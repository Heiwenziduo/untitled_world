package com.github.nahnullscience.cypher_nexus.content.cypher.projectile

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttributeOperation
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractCypherProjectile
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import net.minecraft.core.Holder
import net.minecraft.world.entity.EntityType
import java.util.function.Supplier

class SimpleProjectile(
    val path: String,
    val type: Supplier<out EntityType<out AbstractCypherProjectile>>
) : CypherDataMap.Builder() {
    init {
        manaDrain(1f)
        draw(0)
    }
    private var _color: Int = 0

    private val projectileAttrHolder: HashMap<Holder<CypherAttribute>, Double> = HashMap()
    private val stateChunkHolder: HashMap<Holder<CypherAttribute>, HashMap<CypherAttributeOperation, Double>> = HashMap()

    override fun manaDrain(float: Float) = run { super.manaDrain(float); this }
    override fun draw(int: Int) = run { super.draw(int); this }
    override fun delay(int: Int) = run { super.delay(int); this }
    override fun recharge(int: Int) = run { super.recharge(int); this }
    override fun flags(vararg flag: CypherFlags) = run { super.flags(*flag); this }
    fun color(int: Int) = run { _color = int; this }

    override fun projectileAttr(holder: Holder<CypherAttribute>, value: Double) = run {
        projectileAttrHolder[holder] = value
        this@SimpleProjectile
    }
    override fun stateChunkAttr(holder: Holder<CypherAttribute>, operator: CypherAttributeOperation, value: Double) = run {
        val opMap = stateChunkHolder.getOrPut(holder) { HashMap() }
        opMap[operator] = value
        this@SimpleProjectile
    }


    fun createProjectile() = object : ProjectileCypher() {
        override val resource = CypherNexus.modResource(path)
        override val projectileType = type
        override val color = _color
        override fun defaultAttributes() = this@SimpleProjectile
    }

    override fun build(): CypherDataMap {
        projectileAttrHolder.forEach { (holder, d) ->
            super.projectileAttr(holder, d)
        }
        stateChunkHolder.forEach { (holder, opMap) ->
            opMap.forEach { (op, d) ->
                super.stateChunkAttr(holder, op, d)
            }
        }
        return super.build()
    }
}