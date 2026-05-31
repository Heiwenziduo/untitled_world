package com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import java.util.Locale.getDefault


enum class CypherAttributeOperation {
    /**
     * Base value of one cast, mostly on ProjectileCyphers,
     * without base value, the attribute will be ignored.
     * Can be set via special ModifierCyphers.
     * */
    BASE {
        override fun cumulate(last: Double, new: Double): Double {
            return new // we can assume if another BASE passed in, this only happens when another ConsumerCypher is called
        }
        override fun formatString() = ""
    },
    /**  */
    SET_SELF { // TODO
        override fun cumulate(last: Double, new: Double): Double = new
        override fun formatString() = "="
    },

    /** 1.0 -> add 1.0 */
    ADD {
        override fun cumulate(last: Double, new: Double): Double = last + new
        override fun formatString() = "+"
        override fun format(value: Double): MutableComponent {
            return if (value > 0) super.format(value) else Component.literal("$value")
        }
    },
    /** 0.33 -> plus 33% */
    MULTIPLY_BASE {
        // override val defaultValue = 1.0
        // defaultValue may cumulate multiple times while map initialization(at AbsCypher & Helper)
        override fun cumulate(last: Double, new: Double): Double = last + new
        override fun formatString() = "+"
        override fun format(value: Double): MutableComponent {
            return super.format(value * 100).append("%")
        }
    },
    /** 0.33 -> times 33% */
    MULTIPLY_TOTAL {
        override val defaultValue = 1.0
        override fun cumulate(last: Double, new: Double): Double = last * new
        override fun formatString() = "x"
    },
    /**
     * Force an attribute to become an invariable value,
     * will ignore other operations.
     * */
    SET_ALL {
        override fun cumulate(last: Double, new: Double): Double = new
        override fun formatString() = "="
    },




    ;
//    abstract fun <T> apply(v1: T, v2: T) : T
    open val defaultValue: Double = 0.0
    abstract fun cumulate(last: Double, new: Double) : Double
    abstract fun formatString() : String
    open fun format(value: Double) : MutableComponent = Component.literal("${formatString()}$value")

    override fun toString() = super.toString().lowercase(getDefault())

    companion object {
        fun string2operator(string: String) : CypherAttributeOperation {
            return when(string) {
                "base" -> BASE
                "set_self" -> SET_SELF
                "add" -> ADD
                "multiply_base" -> MULTIPLY_BASE
                "multiply_total" -> MULTIPLY_TOTAL
                "set_all" -> SET_ALL
                else -> throw IllegalArgumentException("$string is not a valid operator, valid operators are: ${entries.toList().map{ operation -> "$operation" }}")
            }
        }

        // Given a string codec to convert to a integer
        // Not all strings can become integers (A is not fully equivalent to B)
        // All integers can become strings (B is fully equivalent to A)
        val CODEC_OPERATION: Codec<CypherAttributeOperation> = Codec.STRING.comapFlatMap(
            { s ->
                try {
                    return@comapFlatMap DataResult.success(string2operator(s))
                } catch (e: IllegalArgumentException) {
                    return@comapFlatMap DataResult.error { "$s is not a valid operator" }
                }
            },
            CypherAttributeOperation::toString
        )
    }
}