package com.github.nahnullscience.cypher_nexus.content.cypher

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.StaticProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.AttributeOperator
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractDedicatedCypherProjectile
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.TriggerType
import net.minecraft.core.Holder
import net.minecraft.world.entity.EntityType
import java.util.EnumMap
import java.util.function.Supplier

/**
 * the idea is, put entity-specific logics inside those Entity [com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractDedicatedCypherProjectile] classes, and leave the cypher simple
 * */
abstract class AbstractSimpleProjectile <out C : AbstractProjectileCypher<AbstractDedicatedCypherProjectile>> (
    protected val path: String,
    protected val type: Supplier<out EntityType<out AbstractDedicatedCypherProjectile>>
) : CypherDataMap.Builder() {
    init {
        manaDrain(1f)
        draw(0)
    }
    var trigger: TriggerType = TriggerType.NONE
        private set
    var triggerCount: Int = 1
        private set
    var color: Int? = null
        private set

    private val projectileAttrHolder: HashMap<Holder<CypherAttribute>, Double> = HashMap()
    private val shotStateAttrHolder: HashMap<Holder<CypherAttribute>, EnumMap<AttributeOperator, Double>> = HashMap()

    override fun manaDrain(float: Float) = apply { super.manaDrain(float) }
    override fun draw(int: Int) = apply { super.draw(int) }
    override fun delay(int: Int) = apply { super.delay(int) }
    override fun recharge(int: Int) = apply { super.recharge(int) }
    override fun flags(vararg flag: CypherFlags) = apply { super.flags(*flag) }
    fun color(int: Int) = apply { color = int }
    fun trigger(type: TriggerType, count: Int = 1) = apply { trigger = type; triggerCount = count }

    override fun projectileAttr(holder: Holder<CypherAttribute>, value: Double) = apply { projectileAttrHolder[holder] = value }
    override fun shotStateAttr(holder: Holder<CypherAttribute>, operator: AttributeOperator, value: Double) = run {
        val opMap = shotStateAttrHolder.getOrPut(holder) { EnumMap(AttributeOperator::class.java) }
        opMap[operator] = value
        this@AbstractSimpleProjectile
    }

    abstract fun createProjectile(): C

    override fun build(): CypherDataMap {
        projectileAttrHolder.forEach { (holder, d) ->
            super.projectileAttr(holder, d)
        }
        shotStateAttrHolder.forEach { (holder, opMap) ->
            opMap.forEach { (op, d) ->
                super.shotStateAttr(holder, op, d)
            }
        }
        return super.build()
    }



    class SimpleProjectile(
        path: String,
        type: Supplier<out EntityType<out AbstractDedicatedCypherProjectile>>
    ) : AbstractSimpleProjectile <ProjectileCypher<AbstractDedicatedCypherProjectile>> (path, type) {
        override fun createProjectile() = object : ProjectileCypher<AbstractDedicatedCypherProjectile>() {
            override val resource = CypherNexus.modResource(path)
            override val projectileType = type
            override val color = this@SimpleProjectile.color
            override val builtinTrigger = this@SimpleProjectile.trigger
            override val builtinTriggerCharge = this@SimpleProjectile.triggerCount
            override fun defaultAttributes() = this@SimpleProjectile
        }
    }

    class SimpleStaticProjectile(
        path: String,
        type: Supplier<out EntityType<out AbstractDedicatedCypherProjectile>>
    ) : AbstractSimpleProjectile <StaticProjectileCypher<AbstractDedicatedCypherProjectile>> (path, type) {
        override fun createProjectile() = object : StaticProjectileCypher<AbstractDedicatedCypherProjectile>() {
            override val resource = CypherNexus.modResource(path)
            override val projectileType = type
            override val color = this@SimpleStaticProjectile.color
            override val builtinTrigger = this@SimpleStaticProjectile.trigger
            override val builtinTriggerCharge = this@SimpleStaticProjectile.triggerCount
            override fun defaultAttributes() = this@SimpleStaticProjectile
        }
    }
}