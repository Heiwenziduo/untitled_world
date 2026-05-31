package com.github.nahnullscience.cypher_nexus.mechanic.cypher

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttributeOperation
import com.github.nahnullscience.cypher_nexus.utility.mod.CNCodecs.CYPHER_ATTRIBUTE
import com.github.nahnullscience.cypher_nexus.utility.mod.CNCodecs.CYPHER_OPERATION_MAP
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.Holder

data class CypherDataAttach(
    val manaDrain: Float,
    val draw: Int,
    val delay: Int,
    val recharge: Int,

    val projectile: Map<CypherAttribute, Map<CypherAttributeOperation, Double>>,
    val stateChunk: Map<CypherAttribute, Map<CypherAttributeOperation, Double>>,
) {
    companion object {
        val CODEC: Codec<CypherDataAttach> = RecordCodecBuilder.create() { it.group(
            Codec.FLOAT.fieldOf("manaDrain").forGetter(CypherDataAttach::manaDrain),
            Codec.intRange(0, 99).fieldOf("draw").forGetter(CypherDataAttach::draw),
            Codec.INT.fieldOf("delay").forGetter(CypherDataAttach::delay),
            Codec.INT.fieldOf("recharge").forGetter(CypherDataAttach::recharge),
            Codec.unboundedMap(CYPHER_ATTRIBUTE, CYPHER_OPERATION_MAP)
                    .fieldOf("projectile").forGetter(CypherDataAttach::projectile),
            Codec.unboundedMap(CYPHER_ATTRIBUTE, CYPHER_OPERATION_MAP)
                    .fieldOf("stateChunk").forGetter(CypherDataAttach::stateChunk),
        ).apply(it, ::CypherDataAttach) }

        val CODEC_STREAM = CODEC

        fun builder() = Builder()
    }

    class Builder() {
        private var manaDrain: Float = 0f
        private var draw: Int? = null
        private var delay: Int? = null
        private var recharge: Int? = null
        private val projectile: HashMap<CypherAttribute, HashMap<CypherAttributeOperation, Double>> = HashMap()
        private val stateChunk: HashMap<CypherAttribute, HashMap<CypherAttributeOperation, Double>> = HashMap()

        fun manaDrain(float: Float): Builder = run { manaDrain = float; this@Builder }
        fun draw(int: Int): Builder = run { draw = int; this@Builder }
        fun delay(int: Int): Builder = run { delay = int; this@Builder }
        fun recharge(int: Int): Builder = run { recharge = int; this@Builder }

        // it seems datagen has a special lifecycle that can unpacks a holder directly
        fun projectileAttr(attr: Holder<CypherAttribute>, value: Double) = projectileAttr(attr.value(), value)
        fun projectileAttr(attr: CypherAttribute, value: Double): Builder {
            val opMap = projectile.getOrPut(attr) { HashMap() }
            opMap[CypherAttributeOperation.BASE] = value
            return this
        }

        fun stateChunkAttr(attr: Holder<CypherAttribute>, operator: CypherAttributeOperation, value: Double) = stateChunkAttr(attr.value(), operator, value)
        fun stateChunkAttr(attr: CypherAttribute, operator: CypherAttributeOperation, value: Double): Builder {
            val opMap = projectile.getOrPut(attr) { HashMap() }
            opMap[operator] = value
            return this
        }

        fun build(): CypherDataAttach = CypherDataAttach(manaDrain, draw?:0, delay?:0, recharge?:0, projectile, stateChunk)
    }
}