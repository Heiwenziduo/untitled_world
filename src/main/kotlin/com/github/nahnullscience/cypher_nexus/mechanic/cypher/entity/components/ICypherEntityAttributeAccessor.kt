package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components

import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity.Companion.cypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntityAttributeAccessor.Companion.getAttrBaseOrNull
import net.minecraft.core.Holder

interface ICypherEntityAttributeAccessor {

    fun hasModifiedAttribute(): Boolean
    fun hasModifiedAttribute(attr: CypherAttribute): Boolean
    fun hasModifiedAttribute(holer: Holder<CypherAttribute>): Boolean

    /**
     * get a modified attribute or null if untouched, should note that null return doesn't mean
     * the projectile not has the given attribute.
     * @see getAttrBaseOrNull
     * */
    fun getAttribute(attr: CypherAttribute): Double?
    /**
     * get a modified attribute or null if untouched, should note that null return doesn't mean
     * the projectile not has the given attribute.
     * @see getAttrBaseOrNull
     * */
    fun getAttribute(holer: Holder<CypherAttribute>): Double?
    /**
     * change the value of the given attribute, this won't sync to another side.
     * @return the old value, or null if there isn't.
     * */
    fun setAttribute(attr: CypherAttribute, value: Double): Double?
    /**
     * change the value of the given attribute, this won't sync to another side.
     * @return the old value, or null if there isn't.
     * */
    fun setAttribute(holer: Holder<CypherAttribute>, value: Double): Double?
//    /**
//     * get value through entity-specific map > cypher default > attribute default
//     * */
//    fun getAttributeOrDefault(attr: CypherAttribute): Double
//    /**
//     * get value through entity-specific map > cypher default > attribute default
//     * */
//    fun getAttributeOrDefault(holer: Holder<CypherAttribute>): Double
//    /**
//     * @return the unmodified base attribute value of the entity if any
//     * @see [AbstractProjectileCypher.getAttrBaseOrNull]
//     * */
//    fun getAttrBaseOrNull(attr: CypherAttribute): Double?
//    /**
//     * @return the unmodified base attribute value of the entity if any
//     * @see [AbstractProjectileCypher.getAttrBaseOrNull]
//     * */
//    fun getAttrBaseOrNull(holder: Holder<CypherAttribute>): Double?


    /**
     * print modified AttrMap.
     * */
    fun debugAttributes()


    companion object {
        fun ICypherEntity.getAttributeOrDefault(holer: Holder<CypherAttribute>) = getAttribute(holer) ?: cypher.getAttrBaseOrDefault(holer)

        fun ICypherEntity.getAttrBaseOrNull(holer: Holder<CypherAttribute>) = cypher.getAttrBaseOrNull(holer)

        inline fun ICypherEntity.computeAttribute(holer: Holder<CypherAttribute>, formular: (current: Double) -> Double) {
            val current = getAttributeOrDefault(holer)
            setAttribute(holer, formular(current))
        }

        inline fun ICypherEntity.computeAttributeIfPresent(holer: Holder<CypherAttribute>, formular: (current: Double) -> Double) {
            val current = getAttribute(holer) ?: return
            setAttribute(holer, formular(current))
        }

        inline fun ICypherEntity.computeBaseAttribute(holer: Holder<CypherAttribute>, formular: (default: Double) -> Double) {
            val current = getAttrBaseOrNull(holer) ?: holer.value().defaultValue
            setAttribute(holer, formular(current))
        }

        fun ICypherEntity.getExisting(): Int = getAttributeOrDefault(CypherAttributes.EXISTING).toInt()

        fun ICypherEntity.getBounce(): Int = getAttributeOrDefault(CypherAttributes.BOUNCE).toInt()

        fun ICypherEntity.getInitialSpeed(): Double = getAttributeOrDefault(CypherAttributes.SPEED)

        fun ICypherEntity.getGravityFactor(): Double = getAttributeOrDefault(CypherAttributes.GRAVITY_FACTOR)

        fun ICypherEntity.getSpeedFactor(): Double = 1f - getAttributeOrDefault(CypherAttributes.FRICTION_FACTOR)

        fun ICypherEntity.getEffectRadius(): Float = getAttributeOrDefault(CypherAttributes.EFFECT_RADIUS).toFloat()

        fun ICypherEntity.getDamage(): Float = getAttributeOrDefault(CypherAttributes.DAMAGE).toFloat()
    }
}