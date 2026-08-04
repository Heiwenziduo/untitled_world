package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk
import net.minecraft.core.Holder

interface ICypherEntityAttributeAccessor {

    fun initAttribute(cypher: AbstractProjectileCypher<*>, shotState: ShotStateChunk)

    fun getAttribute(attr: CypherAttribute): Double?
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

    /**
     * get value through entity-specific map > cypher default > attribute default
     * */
    fun getAttributeOrDefault(attr: CypherAttribute): Double
    /**
     * get value through entity-specific map > cypher default > attribute default
     * */
    fun getAttributeOrDefault(holer: Holder<CypherAttribute>): Double
    /**
     * @return the unmodified base attribute value of the entity if any
     * @see [AbstractProjectileCypher.getAttrBaseOrNull]
     * */
    fun getAttrBaseOrNull(holder: Holder<CypherAttribute>): Double?
    /**
     * @return the unmodified base attribute value of the entity if any
     * @see [AbstractProjectileCypher.getAttrBaseOrNull]
     * */
    fun getAttrBaseOrNull(attr: CypherAttribute): Double?

    companion object {
        inline fun ICypherEntityAttributeAccessor.computeAttribute(holer: Holder<CypherAttribute>, formular: (current: Double) -> Double) {
            val current = getAttributeOrDefault(holer)
            setAttribute(holer, formular(current))
        }

        inline fun ICypherEntityAttributeAccessor.computeAttributeIfPresent(holer: Holder<CypherAttribute>, formular: (current: Double) -> Double) {
            val current = getAttribute(holer) ?: return
            setAttribute(holer, formular(current))
        }

        inline fun ICypherEntityAttributeAccessor.computeAttributeWithDefault(holer: Holder<CypherAttribute>, formular: (default: Double) -> Double) {
            val current = getAttrBaseOrNull(holer) ?: holer.value().defaultValue
            setAttribute(holer, formular(current))
        }
    }
}