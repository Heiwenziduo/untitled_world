package com.github.nahnullscience.cypher_nexus.mechanic.cypher

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.AttributeOperator
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import com.github.nahnullscience.cypher_nexus.utility.mod.CNCodecs.CYPHER_ATTRIBUTE
import com.github.nahnullscience.cypher_nexus.utility.mod.CNCodecs.CYPHER_OPERATION_MAP
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.Holder
import java.util.EnumMap

/** attributes JSON config */
data class CypherDataMap(
    val manaDrain: Float,
    val draw: Int,
    val delay: Int,
    val recharge: Int,
    val flags: Int,

    val projectile: Map<CypherAttribute, Double>,
    val shotState: Map<CypherAttribute, EnumMap<AttributeOperator, Double>>,
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
                    .fieldOf("shotState").orElse(HashMap()).forGetter(CypherDataMap::shotState),
        ).apply(it, ::CypherDataMap) }

        val CODEC_SYNC = CODEC

        fun builder() = Builder()
    }

    open class Builder {
        var manaDrain: Float = 0f
            private set
        var draw: Int? = null
            private set
        var delay: Int? = null
            private set
        var recharge: Int? = null
            private set
        var flags: Int = 0
            private set
        private val projectile: HashMap<CypherAttribute, Double> = HashMap()
        private val shotState: HashMap<CypherAttribute, EnumMap<AttributeOperator, Double>> = HashMap()

        open fun manaDrain(float: Float): Builder = apply { manaDrain = float }
        open fun draw(int: Int): Builder = apply { draw = int }
        open fun delay(int: Int): Builder = apply { delay = int }
        open fun recharge(int: Int): Builder = apply { recharge = int }
        open fun flags(vararg flag: CypherFlags) = apply { flag.forEach { flags = flags or it.value } }

        // it seems datagen has a special lifecycle that can unpacks a holder directly (?)
        open fun projectileAttr(holder: Holder<CypherAttribute>, value: Double) = projectileAttr(holder.value(), value)
        open fun projectileAttr(attr: CypherAttribute, value: Double): Builder = apply { projectile[attr] = value }

        open fun shotStateAttr(holder: Holder<CypherAttribute>, operator: AttributeOperator, value: Double) = shotStateAttr(holder.value(), operator, value)
        open fun shotStateAttr(attr: CypherAttribute, operator: AttributeOperator, value: Double): Builder = apply {
            val opMap = shotState.getOrPut(attr) { EnumMap(AttributeOperator::class.java) }
            opMap[operator] = value
        }

        open fun build(): CypherDataMap = CypherDataMap(
            manaDrain,
            draw ?: 0,
            delay ?: 0,
            recharge ?: 0,
            flags,
            projectile,
            shotState
        )
    }
}