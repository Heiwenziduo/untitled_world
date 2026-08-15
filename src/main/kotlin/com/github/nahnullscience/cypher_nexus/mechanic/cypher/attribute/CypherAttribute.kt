package com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute

import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.utility.i.IRegisterable
import net.minecraft.core.Holder
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.Identifier
import java.text.DecimalFormat

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
    val parser: (Double) -> Double,
    val formatter: DecimalFormat?
): IRegisterable {
    companion object {
        private val self: (Double) -> Double = { it }
    }

    val isAttributeForEntity = applyOn == AttributeApply.ENTITY

    fun restrictRange(v: Double) = v.coerceIn(min, max)

    fun holder(): Holder<CypherAttribute> {
//        val resourceKeyOptional = CypherAttributes.REGISTRY.getResourceKey(attr)
//        return resourceKeyOptional.flatMap { CypherAttributes.REGISTRY.getHolder(resourceKeyOptional) }.orElse(null)
        return CypherAttributes.REGISTRY.get(resource).get() // if this throw, means the attr is not registered
    }

    // ==========================================================================================================

    override fun toString(): String = "attribute_${resource.path}"

    /** lang-JSON key: cypher.attribute.{MOD_ID}.{attribute_name} */
    override fun translation(): MutableComponent = Component.translatable(translationKey)
    private val translationKey = "cypher.attribute.${resource.namespace}.${resource.path}"

    private val unitKey = "gui.${resource.namespace}.cypher.property.${resource.path}.unit"
    fun parseUnit(value: Double) = parser(value)
    /** wrap a given value with the unit of this attribute, for example, `seconds`. not all attributes require a unit */
    fun wrapWithUnit(value: String) = Component.translatableWithFallback(unitKey, value, value)

    /** gui.{MOD_ID}.cypher.property.{attribute_name} */
    fun displayRow(value: MutableComponent): MutableComponent = Component.translatable(displayKey, value)
    private val displayKey = "gui.${resource.namespace}.cypher.property.${resource.path}"


    enum class AttributeApply {
        /**
         * Invoking attributes will not cumulate on cypher-entity,
         * an invoking-attribute indicates some effects only affect how the cypher-entity would spawn,
         * and will not affect how the entity behavior afterward, like `Spread`
         * */
        INVOKING,
        /**
         * only cumulate on the ROOT shot state,
         * a root-attribute indicates instantly inflict some effects to the invoker, like `Recoil`
         * */
        INVOKING_ROOT,
        /**
         * most attribute is an entity-attribute, this indicates the behavior of the entity is somehow affected
         * by its value, like `Gravity`
         * */
        ENTITY
    }

    class Builder(val resource: Identifier) {
        private var defaultValue: Double = 0.0
        private var min: Double = 0.0
        private var max: Double = Double.MAX_VALUE
        private var sync: Boolean = true
        private var applyOn: AttributeApply = AttributeApply.ENTITY
        private var hide: Boolean = false
        private var parser: ((Double) -> Double)? = null
        private var formater: DecimalFormat? = null
        fun build() = CypherAttribute(resource, defaultValue, min, max, sync, applyOn, hide, parser ?: self, formater)
        fun default(value: Double): Builder = apply { defaultValue = value }
        fun min(value: Double): Builder = apply { min = value }
        fun max(value: Double): Builder = apply { max = value }
        fun noSync(): Builder = apply { sync = false }
        fun applyOn(value: AttributeApply): Builder = apply { applyOn = value }
        fun hide(): Builder = apply { hide = true }
        fun parse(parser: (Double) -> Double) = apply { this.parser = parser }
        fun format(formatter: DecimalFormat) = apply { this.formater = formatter }
    }
}