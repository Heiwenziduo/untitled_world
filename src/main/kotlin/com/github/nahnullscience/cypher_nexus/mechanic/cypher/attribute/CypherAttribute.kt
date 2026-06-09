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
    val isProjectileAttribute: Boolean
        get() = applyOn == AttributeApply.PROJECTILE

    init {
        // UntitledWorld.LOGGER.debug("CypherAttribute created: {}", resource.toString())
    }

    fun restrictRange(v: Double) = max(min(max, v), min)


    // ==========================================================================================================
    fun holder(): Holder<CypherAttribute> {
//        val resourceKeyOptional = CypherAttributes.REGISTRY.getResourceKey(attr)
//        return resourceKeyOptional.flatMap { CypherAttributes.REGISTRY.getHolder(resourceKeyOptional) }.orElse(null)
        return CypherAttributes.REGISTRY.get(resource).get() // if this throw, means the attr is not registered
    }

    override fun toString(): String {
        return "attribute_${resource.path}"
    }

    /** lang-JSON key: cypher.attribute.{MOD_ID}.{attribute_name} */
    override fun translation(): MutableComponent =
        Component.translatable("cypher.attribute.${resource.namespace}.${resource.path}")


    enum class AttributeApply {
        INVOKING,
        PROJECTILE
    }

//    class Builder(val resource: ResourceLocation) {
//        val defaultValue: Double = 0.0
//        val min: Double = -Double.MAX_VALUE
//        val max: Double = Double.MAX_VALUE
//        val sync: Boolean = true
//        val applyOn: AttributeApply = AttributeApply.INVOKING
//        val hide: Boolean = false
//        fun build() = CypherAttribute
//    }
}