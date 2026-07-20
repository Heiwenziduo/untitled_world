package com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import java.util.*
import java.util.Locale.getDefault
import kotlin.math.min
import kotlin.math.pow


enum class AttributeOperator(
    val exclusive: Boolean
) {
//    /**
//     * Base value of one cast, mostly on ProjectileCyphers,
//     * without base value, the attribute will be ignored.
//     * Can be set via special ModifierCyphers.
//     * */
//    BASE(false) {
//        override val defaultValue = 0.0
//        override fun cumulate(last: Double, new: Double): Double = new // this should not happen
//        override fun cumulate(last: Double, new: Double, times: Int): Double = cumulate(last, new)
//        override fun formatSymbol() = ""
//    },
//    /**
//     * specify self-set inside each projectile-entities */
//    SET_SELF {
//        override fun cumulate(last: Double, new: Double): Double = new
//        override fun formatString() = "="
//    },

    /** 1.0 -> add 1.0 */
    ADD(false) {
        override val defaultValue = 0.0
        override fun cumulate(last: Double, new: Double): Double = last + new
        override fun cumulate(last: Double, new: Double, times: Int): Double = last + new * times
        override fun formatSymbol() = "+"
        override fun format(value: Double): MutableComponent {
            return if (value > 0) super.format(value) else Component.literal("$value")
        }
    },
    /** 0.33 -> plus 33% */
    MULTIPLY_BASE(false) {
        // defaultValue may cumulate multiple times while map initialization(at AbsCypher & Helper)
        override val defaultValue = 0.0
        override fun cumulate(last: Double, new: Double): Double = last + new
        override fun cumulate(last: Double, new: Double, times: Int): Double = last + new * times
        override fun formatSymbol() = "+"
        override fun format(value: Double): MutableComponent {
            return super.format(value * 100).append("%")
        }
    },
    /** 0.33 -> times 33% */
    MULTIPLY_TOTAL(false) {
        override val defaultValue = 1.0
        override fun cumulate(last: Double, new: Double): Double = last * new
        override fun cumulate(last: Double, new: Double, times: Int): Double = last * new.pow(times)
        override fun formatSymbol() = "x"
    },
    /**
     * Force an attribute to become an invariable value,
     * will ignore other operations.
     * */
    SET_ALL(true) {
        override val defaultValue = 0.0
        override fun cumulate(last: Double, new: Double): Double = new
        override fun cumulate(last: Double, new: Double, times: Int): Double = new
        override fun formatSymbol() = "="
    },

    CAP_AT(false) {
        override val defaultValue = Double.MAX_VALUE
        override fun cumulate(last: Double, new: Double): Double = min(last, new)
        override fun cumulate(last: Double, new: Double, times: Int): Double = min(last, new)
        override fun formatSymbol() = "<"
    }


    ;
//    abstract fun <T> apply(v1: T, v2: T) : T
    abstract val defaultValue: Double
    abstract fun cumulate(last: Double, new: Double) : Double
    abstract fun cumulate(last: Double, new: Double, times: Int) : Double
    abstract fun formatSymbol() : String

    open fun format(value: Double) : MutableComponent = Component.literal("${formatSymbol()}$value")
    override fun toString() = super.toString().lowercase(getDefault())

    companion object {

        typealias OperatorMap = EnumMap<AttributeOperator, Double>
        /**
         * calculate attribute value in vanilla style
         * @param opMap contains pre-computed values for each [AttributeOperator],
         * should champion [EnumMap] over [HashMap] for faster key access
         * @return the calculation result, since the method don't care about which `Attribute` is calculated,
         * you should perform range restriction manually
         * */
        fun attributeCalculator(base: Double, opMap: Map<AttributeOperator, Double>) : Double {
            val s = opMap[SET_ALL]
            if (s != null) return s

            val a = opMap.getOrDefault(ADD, ADD.defaultValue)
            val m1 = opMap.getOrDefault(MULTIPLY_BASE, MULTIPLY_BASE.defaultValue)
            val m2 = opMap.getOrDefault(MULTIPLY_TOTAL, MULTIPLY_TOTAL.defaultValue)
            val cap = opMap.getOrDefault(CAP_AT, CAP_AT.defaultValue)
            return ((base + a) * (m1 + 1) * m2).coerceAtMost(cap)
        }

        typealias AttributeMap = MutableMap<CypherAttribute, Double>
        /**
         * cumulate attributes from a shot-state to a single cypher-entity.
         * @param [AttributeMap] where `Attribute`s will cumulate to.
         * Despite the specification of the broad `MutableMap` type,
         * [Reference2ObjectOpenHashMap] should be preferred over a standard `HashMap` for performance reasons.
         * */
        fun AttributeMap.initFromShotState(state: ShotStateChunk, cypher: AbstractProjectileCypher<*>) {
            state.attr2opMap.forEach { (attr, opMap) ->
                if (!attr.isEntityAttribute) return@forEach
//            if (haveFlag(CypherFlags.CONSTANT_EXISTING) && CypherAttributes.EXISTING.`is`(attr.resource)) return@forEach
                // TODO prune cumulation, some of attributes will not be used, depends on cypher implementation

                this.compute(attr) { a, v ->
                    val base = cypher.getAttrBaseOrDefault(attr)
                    val final = this@Companion.attributeCalculator(base, opMap)
                    attr.restrictRange(final)
                }
            }
        }


        fun string2operator(string: String) : AttributeOperator {
            return when(string) {
                "add" -> ADD
                "multiply_base" -> MULTIPLY_BASE
                "multiply_total" -> MULTIPLY_TOTAL
                "set_all" -> SET_ALL
                "cap_at" -> CAP_AT
                else -> throw IllegalArgumentException("$string is not a valid operator, valid operators are: ${entries.toList().map{ operation -> "$operation" }}")
            }
        }

        // Given a string codec to convert to a integer
        // Not all strings can become integers (A is not fully equivalent to B)
        // All integers can become strings (B is fully equivalent to A)
        val CODEC_OPERATION: Codec<AttributeOperator> = Codec.STRING.comapFlatMap(
            { s ->
                try {
                    return@comapFlatMap DataResult.success(string2operator(s))
                } catch (e: IllegalArgumentException) {
                    return@comapFlatMap DataResult.error { "$s is not a valid operator" }
                }
            },
            AttributeOperator::toString
        )
    }
}