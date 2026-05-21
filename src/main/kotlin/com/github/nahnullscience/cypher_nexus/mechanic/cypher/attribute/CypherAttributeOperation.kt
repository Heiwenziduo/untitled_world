package com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

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
        override fun toString() = ""
    },
    /** 1.0 -> add 1.0 */
    ADD {
        override fun cumulate(last: Double, new: Double): Double = last + new
        override fun toString() = "+"
        override fun format(value: Double): MutableComponent {
            return if (value > 0) super.format(value) else Component.literal("$value")
        }
    },
    /** 0.33 -> plus 33% */
    MULTIPLY_BASE {
        // override val defaultValue = 1.0
        // defaultValue may cumulate multiple times while map initialization(at AbsCypher & Helper)
        override fun cumulate(last: Double, new: Double): Double = last + new
        override fun toString() = "+"
        override fun format(value: Double): MutableComponent {
            return super.format(value * 100).append("%")
        }
    },
    /** 0.33 -> times 33% */
    MULTIPLY_TOTAL {
        override val defaultValue = 1.0
        override fun cumulate(last: Double, new: Double): Double = last * new
        override fun toString() = "x"
    },
    /**
     * Force an attribute to become an invariable value,
     * will ignore other operations.
     * */
    SET_ALL {
        override fun cumulate(last: Double, new: Double): Double = new
        override fun toString() = "="
    },


    SET_SELF { // TODO
        override fun cumulate(last: Double, new: Double): Double = new
        override fun toString() = "="
    },

    ;
//    abstract fun <T> apply(v1: T, v2: T) : T
    open val defaultValue: Double = 0.0
    abstract fun cumulate(last: Double, new: Double) : Double

    open fun format(value: Double) : MutableComponent = Component.literal("$this$value")
}