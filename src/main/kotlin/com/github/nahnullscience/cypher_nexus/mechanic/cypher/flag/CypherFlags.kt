package com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag

import com.github.nahnullscience.cypher_nexus.utility.i.IFlaggable

/**
 * Kotlin provides a few operators for bits operation
 *
 * shl(bits) – signed shift left
 *
 * shr(bits) – signed shift right
 *
 * ushr(bits) – unsigned shift right
 *
 * and(bits) – bitwise AND
 *
 * or(bits) – bitwise OR
 *
 * xor(bits) – bitwise XOR
 *
 * inv() – bitwise inversion
 * */

/** a flag is basically a bundle of booleans, all flag-bits are 0 by default */
enum class CypherFlags(override val value: Int): IFlaggable.IFlagEnum {
    /** self-explanatory */
    HURT_OWNER(1),
    // TODO deal pierce & bounce logic
    PIERCE_ENTITY(2),
    PIERCE_BLOCK(4),
    /** skip dealing damage process totally */
    NO_DAMAGE(8),
    /** #displayFireAnimation */
    WITH_FIRE(16),
    /** stick on touching surface */
    STICKY(32), // TODO
    /** make projectile always use its base existing, not affect by modifiers */
    CONSTANT_EXISTING(64),


    ;
    init {
        require(value != 0)
        require(value == 1 || value % 2 == 0)
    }

    companion object {
        fun printFlag(flags: Int) {
            print("number $flags have CypherFlag:\n[")
            for (e in entries) {
                if (flags and e.value > 0) print("${e.name}, ")
            }
            println("]")
        }
    }
}