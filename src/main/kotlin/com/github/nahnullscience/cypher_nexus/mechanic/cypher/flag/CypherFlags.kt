package com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag

import com.github.nahnullscience.cypher_nexus.utility.i.IFlagExtension

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
enum class CypherFlags : IFlagExtension.IFlagEnum {

    /**
     * let the projectile able to hurt the invoker self
     * */
    HURT_OWNER,

    /**
     *
     * */
    PIERCE_ENTITY,

    /**
     *
     * */
    IGNORE_BLOCK,

    /**
     * ignore all collision, disable physics
     * */
    PENETRATE_WORLD,

    /**
     * skip dealing damage process totally
     * */
    SKIP_DAMAGE_CHECK,

    /**
     * mark the projectile fire-related
     * */
    // TODO
    WITH_FIRE,

    /**
     * mark the projectile electric-related
     * */
    // TODO
    WITH_ELECTRICITY,

    /**
     * mark the projectile ender-related
     * */
    // TODO
    WITH_ENDER_POWER,

    /**
     * keep existence even speed is low
     * */
    // TODO
    LINGER,

    /**
     * let the projectile movement client-authoritative, if the owner is client-authoritative
     * */
    MOTION_FOLLOWS_OWNER,

    /**
     * let the projectile explode when disappear
     * */
    // TODO
    EXPLOSIVE,

    /**
     * do not play server sound
     * */
    SILENT,

    /**
     * always render in full light
     * */
    GLOWING,

//    /**
//     * disable rotation and save a few triangular computation each tick,
//     * mainly for item-renderer projectiles (like snowball) on which rotations do nothing.
//     *
//     * not mandatory
//     * */
//    NO_ROTATION,

    ;
    override val value: Int = 1 shl ordinal
    init {
//        require(value != 0)
//        require(value == 1 || value % 2 == 0)
        require(value < Int.MAX_VALUE)
    }

    companion object {
        fun printFlag(flags: Int) {
            print("number $flags have CypherFlag:\n[")
            for (e in entries) {
                if (flags and e.value > 0) print("${e.name}, ")
            }
            println("]")
        }

        fun fromFlags(vararg flags: CypherFlags): Int = flags.sumOf { it.value }

        fun Int.containsFlag(flag: CypherFlags): Boolean = this and flag.value == flag.value
    }
}