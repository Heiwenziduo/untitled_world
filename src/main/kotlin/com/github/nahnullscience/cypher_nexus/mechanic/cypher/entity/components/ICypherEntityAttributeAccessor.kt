package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components

import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity.Companion.cypher
import net.minecraft.core.Holder

interface ICypherEntityAttributeAccessor {

    fun hasModifiedAttribute(): Boolean
    fun hasModifiedAttribute(attr: CypherAttribute): Boolean

    /**
     * get the value of an attribute either modified or default, the order of searching default value is
     * cypher-default -> attribute-default
     * */
    fun getAttributeOrDefault(attr: CypherAttribute): Double
    /**
     * change the value of the given attribute, this won't sync to another side.
     * @return the old value, or null if there isn't.
     * */
    fun setAttribute(attr: CypherAttribute, value: Double): Double

    companion object {
        fun ICypherEntityAttributeAccessor.hasModifiedAttribute(holer: Holder<CypherAttribute>): Boolean = hasModifiedAttribute(holer.value())
        fun ICypherEntityAttributeAccessor.getAttributeOrDefault(holer: Holder<CypherAttribute>): Double = getAttributeOrDefault(holer.value())
        fun ICypherEntityAttributeAccessor.setAttribute(holer: Holder<CypherAttribute>, value: Double): Double = setAttribute(holer.value(), value)


        inline fun ICypherEntity.computeAttribute(holer: Holder<CypherAttribute>, formular: (current: Double) -> Double) {
            val current = getAttributeOrDefault(holer)
            setAttribute(holer, formular(current))
        }

        inline fun ICypherEntity.computeAttrIfPresent(holer: Holder<CypherAttribute>, formular: (current: Double) -> Double) {
            val current = if (hasModifiedAttribute(holer)) getAttributeOrDefault(holer) else return
            setAttribute(holer, formular(current))
        }

        inline fun ICypherEntity.computeAttrWithCurrent(holer: Holder<CypherAttribute>, formular: (value: Double) -> Double) =
            computeAttrWithCurrent(holer, holer.value().defaultValue, formular)
        inline fun ICypherEntity.computeAttrWithCurrent(
            holer: Holder<CypherAttribute>,
            fallback: Double,
            formular: (value: Double) -> Double
        ) {
            val attr = if (hasModifiedAttribute(holer)) getAttributeOrDefault(holer) else fallback
            setAttribute(holer, formular(attr))
        }

        inline fun ICypherEntity.computeAttrWithBase(holer: Holder<CypherAttribute>, formular: (value: Double) -> Double) =
            computeAttrWithBase(holer, holer.value().defaultValue, formular)
        inline fun ICypherEntity.computeAttrWithBase(
            holer: Holder<CypherAttribute>,
            fallback: Double,
            formular: (value: Double) -> Double
        ) {
            val attr = holer.value()
            val value = if (cypher.hasAttr(attr)) cypher.getAttrOrDefault(attr) else fallback
            setAttribute(holer, formular(value))
        }

        fun ICypherEntity.getExisting(): Int = this@getExisting.getAttributeOrDefault(CypherAttributes.EXISTING).toInt()

        fun ICypherEntity.getBounce(): Int = this@getBounce.getAttributeOrDefault(CypherAttributes.BOUNCE).toInt()

        fun ICypherEntity.getInitialSpeed(): Double = this@getInitialSpeed.getAttributeOrDefault(CypherAttributes.SPEED)

        fun ICypherEntity.getGravityFactor(): Double = this@getGravityFactor.getAttributeOrDefault(CypherAttributes.GRAVITY_FACTOR)

        fun ICypherEntity.getSpeedFactor(): Double = 1f - this@getSpeedFactor.getAttributeOrDefault(CypherAttributes.FRICTION_FACTOR)

        fun ICypherEntity.getEffectRadius(): Float = this@getEffectRadius.getAttributeOrDefault(CypherAttributes.EFFECT_RADIUS).toFloat()

        fun ICypherEntity.getDamage(): Float = this@getDamage.getAttributeOrDefault(CypherAttributes.DAMAGE).toFloat()
    }
}