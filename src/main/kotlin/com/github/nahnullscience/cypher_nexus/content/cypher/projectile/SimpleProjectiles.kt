package com.github.nahnullscience.cypher_nexus.content.cypher.projectile

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.StaticProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.AttributeOperator
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.DedicatedCypherProjectile
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.TriggerType
import net.minecraft.core.Holder
import net.minecraft.world.entity.EntityType
import java.util.EnumMap
import java.util.function.Supplier

/**
 * the idea is, put entity-specific logics inside those Entity [DedicatedCypherProjectile] classes, and leave the cypher simple
 * */
abstract class SimpleProjectiles <out C : AbstractProjectileCypher<DedicatedCypherProjectile>> (
    protected val path: String,
    protected val type: Supplier<out EntityType<out DedicatedCypherProjectile>>
) : CypherDataMap.Builder() {
    init {
        manaDrain(1f)
        draw(0)
    }
    var trigger: TriggerType = TriggerType.NONE
        private set
    var triggerCount: Int = 1
        private set
    var color: Int = 0
        private set

    private val projectileAttrHolder: HashMap<Holder<CypherAttribute>, Double> = HashMap()
    private val stateChunkHolder: HashMap<Holder<CypherAttribute>, EnumMap<AttributeOperator, Double>> = HashMap()

    override fun manaDrain(float: Float) = run { super.manaDrain(float); this }
    override fun draw(int: Int) = run { super.draw(int); this }
    override fun delay(int: Int) = run { super.delay(int); this }
    override fun recharge(int: Int) = run { super.recharge(int); this }
    override fun flags(vararg flag: CypherFlags) = run { super.flags(*flag); this }
    fun color(int: Int) = run { color = int; this }
    fun trigger(type: TriggerType, count: Int = 1) = apply { trigger = type; triggerCount = count }

    override fun projectileAttr(holder: Holder<CypherAttribute>, value: Double) = run {
        projectileAttrHolder[holder] = value
        this@SimpleProjectiles
    }
    override fun stateChunkAttr(holder: Holder<CypherAttribute>, operator: AttributeOperator, value: Double) = run {
        val opMap = stateChunkHolder.getOrPut(holder) { EnumMap(AttributeOperator::class.java) }
        opMap[operator] = value
        this@SimpleProjectiles
    }

    abstract fun createProjectile(): C

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



    class SimpleProjectile(
        path: String,
        type: Supplier<out EntityType<out DedicatedCypherProjectile>>
    ) : SimpleProjectiles <ProjectileCypher<DedicatedCypherProjectile>> (path, type) {
        override fun createProjectile() = object : ProjectileCypher<DedicatedCypherProjectile>() {
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
        type: Supplier<out EntityType<out DedicatedCypherProjectile>>
    ) : SimpleProjectiles <StaticProjectileCypher<DedicatedCypherProjectile>> (path, type) {
        override fun createProjectile() = object : StaticProjectileCypher<DedicatedCypherProjectile>() {
            override val resource = CypherNexus.modResource(path)
            override val projectileType = type
            override val color = this@SimpleStaticProjectile.color
            override val builtinTrigger = this@SimpleStaticProjectile.trigger
            override val builtinTriggerCharge = this@SimpleStaticProjectile.triggerCount
            override fun defaultAttributes() = this@SimpleStaticProjectile
        }
    }
}