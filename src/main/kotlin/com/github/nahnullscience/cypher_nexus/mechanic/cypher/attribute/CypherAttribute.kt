package com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute

import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.utility.i.IRegisterable
import net.minecraft.core.Holder
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.Identifier
import kotlin.math.max
import kotlin.math.min

/**
 * a bit like vanilla LivingEntity's Attribute system
 * */
open class CypherAttribute(
    override val resource: Identifier,
    val defaultValue: Double,
    val min: Double,
    val max: Double,
    val sync: Boolean = true,
    val applyOn: AttributeApply,
    /** whether the attr will show on tooltips */
    val hide: Boolean = false,
): IRegisterable {
    val isProjectileAttribute: Boolean get() = applyOn == AttributeApply.PROJECTILE

    fun restrictRange(v: Double) = v.coerceIn(min, max)


    // ==========================================================================================================
    fun holder(): Holder<CypherAttribute> {
//        val resourceKeyOptional = CypherAttributes.REGISTRY.getResourceKey(attr)
//        return resourceKeyOptional.flatMap { CypherAttributes.REGISTRY.getHolder(resourceKeyOptional) }.orElse(null)
        return CypherAttributes.REGISTRY.get(resource).get() // if this throw, means the attr is not registered
    }

    override fun toString(): String = "attribute_${resource.path}"

    /** lang-JSON key: cypher.attribute.{MOD_ID}.{attribute_name} */
    override fun translation(): MutableComponent =
        Component.translatable("cypher.attribute.${resource.namespace}.${resource.path}")


    enum class AttributeApply {
        /** Invoking attributes will not cumulate on projectile-entity */
        INVOKING,
        PROJECTILE
    }

    class Builder(val resource: Identifier) {
        private var defaultValue: Double = 0.0
        private var min: Double = 0.0
        private var max: Double = Double.MAX_VALUE
        private var sync: Boolean = true
        private var applyOn: AttributeApply = AttributeApply.PROJECTILE
        private var hide: Boolean = false
        fun build() = CypherAttribute(resource, defaultValue, min, max, sync, applyOn, hide)
        fun default(value: Double): Builder = run { defaultValue = value; this }
        fun min(value: Double): Builder = run { min = value ; this }
        fun max(value: Double): Builder = run { max = value ; this }
        fun notSync(): Builder = run { sync = false ; this }
        fun applyOn(value: AttributeApply): Builder = run { applyOn = value ; this }
        fun hide(): Builder = run { hide = true ; this }
    }
}