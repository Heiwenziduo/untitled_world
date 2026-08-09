//package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation
//
//import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
//import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
//import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk
//import com.github.nahnullscience.cypher_nexus.utility.mod.AttributeFastMap
//import net.minecraft.core.Holder
//
//open class CEAttribute : ICEAttribute {
//    protected val attributeMap = AttributeFastMap()
//
//    override fun initCypher(
//        cypher: AbstractProjectileCypher<*>,
//        shotState: ShotStateChunk?,
//    ) {
//        shotState?.let {
//            attributeMap.initFromShotState(it, cypher)
//        }
//    }
//
//    override fun hasModifiedAttribute(): Boolean = attributeMap.isNotEmpty()
//
//    override fun hasModifiedAttribute(attr: CypherAttribute): Boolean = attributeMap.containsKey(attr)
//    override fun hasModifiedAttribute(holer: Holder<CypherAttribute>): Boolean = hasModifiedAttribute(holer.value())
//
//    override fun getAttributeOrDefault(attr: CypherAttribute): Double = attributeMap[attr]
//    fun getAttributeOrDefault(holer: Holder<CypherAttribute>): Double = getAttributeOrDefault(holer.value())
//
//    override fun setAttribute(attr: CypherAttribute, value: Double): Double = attributeMap.put(attr, value) // return the old
//    override fun setAttribute(holer: Holder<CypherAttribute>, value: Double): Double = setAttribute(holer.value(), value)
//
////    override fun getAttributeOrDefault(attr: CypherAttribute) = attributeMap[attr] ?: cypher.getAttrBaseOrDefault(attr)
////    override fun getAttributeOrDefault(holer: Holder<CypherAttribute>) = getAttributeOrDefault(holer.value())
////
////    override fun getAttrBaseOrNull(attr: CypherAttribute) = cypher.getAttrBaseOrNull(attr)
////    override fun getAttrBaseOrNull(holder: Holder<CypherAttribute>) = getAttrBaseOrNull(holder.value())
//
//    override fun debugAttributes() {
//        println("Attributes: ")
//        if (hasModifiedAttribute())
//        for ((a, d) in attributeMap) {
//            println("$a: $d")
//        }
//        else println("no modified attribute")
//    }
//}