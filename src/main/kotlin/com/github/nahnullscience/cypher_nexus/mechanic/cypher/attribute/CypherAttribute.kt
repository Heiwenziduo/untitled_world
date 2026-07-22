package com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.utility.i.IRegisterable
import net.minecraft.core.Holder
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.Identifier

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

    val isEntityAttribute = applyOn == AttributeApply.ENTITY

    fun restrictRange(v: Double) = v.coerceIn(min, max)

    fun holder(): Holder<CypherAttribute> {
//        val resourceKeyOptional = CypherAttributes.REGISTRY.getResourceKey(attr)
//        return resourceKeyOptional.flatMap { CypherAttributes.REGISTRY.getHolder(resourceKeyOptional) }.orElse(null)
        return CypherAttributes.REGISTRY.get(resource).get() // if this throw, means the attr is not registered
    }

    // ==========================================================================================================

    // TODO add value formatter to Existing Crit Speed

    override fun toString(): String = "attribute_${resource.path}"

    /** lang-JSON key: cypher.attribute.{MOD_ID}.{attribute_name} */
    private val translationKey by lazy { "cypher.attribute.${resource.namespace}.${resource.path}" }
    override fun translation(): MutableComponent = Component.translatable(translationKey)

    /** wrap a given value with the unit of this attribute, for example, `seconds`. not all attributes require a unit */
    private val unitKey by lazy { "gui.${resource.namespace}.cypher.property.${resource.path}.unit" }
    fun wrapWithUnit(value: String) = Component.translatableWithFallback(unitKey, value, value)

    /** gui.{MOD_ID}.cypher.property.{attribute_name} */
    private val displayKey by lazy { "gui.${resource.namespace}.cypher.property.${resource.path}" }
    fun displayRow(value: MutableComponent): MutableComponent = Component.translatable(displayKey, value)


    enum class AttributeApply {
        /** Invoking attributes will not cumulate on projectile-entity */
        INVOKING,
        ENTITY
    }

    class Builder(val resource: Identifier) {
        private var defaultValue: Double = 0.0
        private var min: Double = 0.0
        private var max: Double = Double.MAX_VALUE
        private var sync: Boolean = true
        private var applyOn: AttributeApply = AttributeApply.ENTITY
        private var hide: Boolean = false
        fun build() = CypherAttribute(resource, defaultValue, min, max, sync, applyOn, hide)
        fun default(value: Double): Builder = run { defaultValue = value; this }
        fun min(value: Double): Builder = run { min = value ; this }
        fun max(value: Double): Builder = run { max = value ; this }
        fun noSync(): Builder = run { sync = false ; this }
        fun applyOn(value: AttributeApply): Builder = run { applyOn = value ; this }
        fun hide(): Builder = run { hide = true ; this }
    }
}