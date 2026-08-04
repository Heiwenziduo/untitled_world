package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntityAttributeAccessor
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk
import com.github.nahnullscience.cypher_nexus.utility.mod.AttributeFastMap
import net.minecraft.core.Holder
import kotlin.collections.get

open class CEAttributeAccessor : ICypherEntityAttributeAccessor {
    protected lateinit var attributeMap: AttributeFastMap
    protected lateinit var cypher: AbstractProjectileCypher<*>

    override fun initAttribute(
        cypher: AbstractProjectileCypher<*>,
        shotState: ShotStateChunk,
    ) {
        this.cypher = cypher
        attributeMap = AttributeFastMap().apply { initFromShotState(shotState, cypher) }
    }


    override fun getAttribute(attr: CypherAttribute): Double? = attributeMap[attr]
    override fun getAttribute(holer: Holder<CypherAttribute>): Double? = getAttribute(holer.value())
    override fun setAttribute(attr: CypherAttribute, value: Double): Double? = attributeMap.put(attr, value) // return the old
    override fun setAttribute(holer: Holder<CypherAttribute>, value: Double): Double? = setAttribute(holer.value(), value)

    override fun getAttributeOrDefault(holer: Holder<CypherAttribute>) = getAttributeOrDefault(holer.value())
    override fun getAttributeOrDefault(attr: CypherAttribute) = attributeMap[attr] ?: cypher.getAttrBaseOrDefault(attr)
    override fun getAttrBaseOrNull(holder: Holder<CypherAttribute>) = getAttrBaseOrNull(holder.value())
    override fun getAttrBaseOrNull(attr: CypherAttribute) = cypher.getAttrBaseOrNull(attr)
}