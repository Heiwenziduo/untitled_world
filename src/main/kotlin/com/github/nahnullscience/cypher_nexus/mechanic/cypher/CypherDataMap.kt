package com.github.nahnullscience.cypher_nexus.mechanic.cypher

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttributeOperation
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import com.github.nahnullscience.cypher_nexus.utility.mod.CNCodecs.CYPHER_ATTRIBUTE
import com.github.nahnullscience.cypher_nexus.utility.mod.CNCodecs.CYPHER_OPERATION_MAP
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.Holder

/** attributes JSON config */
data class CypherDataMap(
    val manaDrain: Float,
    val draw: Int,
    val delay: Int,
    val recharge: Int,
    val flags: Int,

    val projectile: Map<CypherAttribute, Double>,
    val stateChunk: Map<CypherAttribute, Map<CypherAttributeOperation, Double>>,
) {
    companion object {
        val CODEC: Codec<CypherDataMap> = RecordCodecBuilder.create() { it.group(
            Codec.FLOAT.fieldOf("manaDrain").forGetter(CypherDataMap::manaDrain),
            Codec.intRange(0, 99).fieldOf("draw").orElse(0).forGetter(CypherDataMap::draw),
            Codec.INT.fieldOf("delay").orElse(0).forGetter(CypherDataMap::delay),
            Codec.INT.fieldOf("recharge").orElse(0).forGetter(CypherDataMap::recharge),
            Codec.INT.fieldOf("flags").orElse(0).forGetter(CypherDataMap::flags),
            Codec.unboundedMap(CYPHER_ATTRIBUTE, Codec.DOUBLE)
                    .fieldOf("projectile").orElse(HashMap()).forGetter(CypherDataMap::projectile),
            Codec.unboundedMap(CYPHER_ATTRIBUTE, CYPHER_OPERATION_MAP)
                    .fieldOf("stateChunk").orElse(HashMap()).forGetter(CypherDataMap::stateChunk),
        ).apply(it, ::CypherDataMap) }

        val CODEC_SYNC = CODEC

        fun builder() = Builder()
    }

    open class Builder {
        private var manaDrain: Float = 0f
        private var draw: Int? = null
        private var delay: Int? = null
        private var recharge: Int? = null
        private var flags: Int = 0
        private val projectile: HashMap<CypherAttribute, Double> = HashMap()
        private val stateChunk: HashMap<CypherAttribute, HashMap<CypherAttributeOperation, Double>> = HashMap()

        open fun manaDrain(float: Float): Builder = run { manaDrain = float; this@Builder }
        open fun draw(int: Int): Builder = run { draw = int; this@Builder }
        open fun delay(int: Int): Builder = run { delay = int; this@Builder }
        open fun recharge(int: Int): Builder = run { recharge = int; this@Builder }
        open fun flags(vararg flag: CypherFlags) = run { flag.forEach { flags = flags or it.value }; this }

        // it seems datagen has a special lifecycle that can unpacks a holder directly (?)
        open fun projectileAttr(holder: Holder<CypherAttribute>, value: Double) = projectileAttr(holder.value(), value)
        open fun projectileAttr(attr: CypherAttribute, value: Double): Builder {
            projectile[attr] = value
            return this
        }

        open fun stateChunkAttr(holder: Holder<CypherAttribute>, operator: CypherAttributeOperation, value: Double) = stateChunkAttr(holder.value(), operator, value)
        open fun stateChunkAttr(attr: CypherAttribute, operator: CypherAttributeOperation, value: Double): Builder {
            val opMap = stateChunk.getOrPut(attr) { HashMap() }
            opMap[operator] = value
            return this
        }

        open fun build(): CypherDataMap = CypherDataMap(
            manaDrain,
            draw ?: 0,
            delay ?: 0,
            recharge ?: 0,
            flags,
            projectile,
            stateChunk
        )
    }
}