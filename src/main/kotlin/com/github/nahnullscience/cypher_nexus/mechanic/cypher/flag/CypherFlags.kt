package com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag

import com.github.nahnullscience.cypher_nexus.utility.i.IFlagExtension


enum class CypherFlags : IFlagExtension.IFlagEnum {

    /**
     * let the projectile able to hurt the invoker self
     * */
    HURT_OWNER,

    /**
     * allow CE pierce through entities, also enables infinite(practically) triggers
     * */
    PIERCE_ENTITY,

    /**
     * allow CE ignore block collision
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
     * with physics,
     * keep existence even speed is low
     * */
    // TODO
    PHYSICS_SOLID,

    /**
     * let the projectile movement client-authoritative, if the owner is client-authoritative
     * */
    MOTION_FOLLOWS_OWNER,

    /**
     * let the projectile explode when disappear
     * */
    EXPLOSIVE,

    /**
     * disable projectile explosion even [EXPLOSIVE] is present
     * */
    REMOVE_EXPLOSION,

    /**
     * make explosions not break blocks
     * */
    SAFE_EXPLODE,

    /**
     * do not play server sound
     * */
    // TODO
    SILENT,

    /**
     * always render in full light
     * */
    GLOWING,

    /**
     * strengthen the juxta series
     * */
    PHANTOM,

    /**
     * make
     * */
    // TODO
    POLYMORPH,


//    /**
//     * disable rotation and save a few triangular computation each tick,
//     * mainly for item-renderer projectiles (like snowball) on which rotations do nothing.
//     *
//     * not mandatory
//     * */
//    NO_ROTATION,

    ;
    override val mask: Int = 1 shl ordinal
    init {
//        require(value != 0)
//        require(value == 1 || value % 2 == 0)
        require(mask < Int.MAX_VALUE)
    }

    companion object {
        fun printFlag(flags: Int) {
            print("number $flags have CypherFlag:\n[")
            for (e in entries) {
                if (flags and e.mask > 0) print("${e.name}, ")
            }
            println("]")
        }

        fun Int.containsFlag(flag: CypherFlags): Boolean = this and flag.mask == flag.mask
    }
}
