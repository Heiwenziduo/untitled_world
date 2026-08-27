package com.github.nahnullscience.cypher_nexus.utility.i

import com.github.nahnullscience.cypher_nexus.utility.i.IFlagExtension.IFlagEnum

/**
 * utilities to store, use, computed flags.
 * a flag is basically a bundle of booleans, all flag-bits are 0 by default.
 * */
interface IFlagExtension <FLAG : IFlagEnum> {

    /**
     * a flag is basically a bundle of booleans
     * */
    var enabledFlags: Int

    fun enableFlag(flag: FLAG) = let { enabledFlags = enabledFlags or flag.mask }
    fun enableFlagRaw(flags: Int) = let { enabledFlags = enabledFlags or flags }

    fun hasFlag(flag: FLAG) = enabledFlags and flag.mask == flag.mask
    fun hasFlagsAny(flag0: FLAG) = hasFlag(flag0)
    fun hasFlagsAny(flag0: FLAG, flag1: FLAG) = hasFlag(flag0) || hasFlag(flag1)
    fun hasFlagsAny(flag0: FLAG, flag1: FLAG, flag2: FLAG) = hasFlag(flag0) || hasFlag(flag1) || hasFlag(flag2)
    fun hasFlagsAny(flag0: FLAG, flag1: FLAG, flag2: FLAG, flag3: FLAG) = hasFlag(flag0) || hasFlag(flag1) || hasFlag(flag2) || hasFlag(flag3)

    fun hasFlagsAll(flag0: FLAG) = hasFlag(flag0)
    fun hasFlagsAll(flag0: FLAG, flag1: FLAG) = hasFlag(flag0) && hasFlag(flag1)
    fun hasFlagsAll(flag0: FLAG, flag1: FLAG, flag2: FLAG) = hasFlag(flag0) && hasFlag(flag1) && hasFlag(flag2)
    fun hasFlagsAll(flag0: FLAG, flag1: FLAG, flag2: FLAG, flag3: FLAG) = hasFlag(flag0) && hasFlag(flag1) && hasFlag(flag2) && hasFlag(flag3)

    fun noFlag(flag: FLAG) = enabledFlags and flag.mask == 0
    fun noFlagsNone(flag0: FLAG) = noFlag(flag0)
    fun noFlagsNone(flag0: FLAG, flag1: FLAG) = noFlag(flag0) && noFlag(flag1)
    fun noFlagsNone(flag0: FLAG, flag1: FLAG, flag2: FLAG) = noFlag(flag0) && noFlag(flag1) && noFlag(flag2)
    fun noFlagsNone(flag0: FLAG, flag1: FLAG, flag2: FLAG, flag3: FLAG) = noFlag(flag0) && noFlag(flag1) && noFlag(flag2) && noFlag(flag3)

    // beware the collection allocation
    /**
     * ANY of given flags are present
     * */
    @Deprecated("")
    fun hasFlagsAny(vararg flags: FLAG) =
        flags.firstOrNull { flag -> hasFlag(flag) } ?.let { true } ?: false
    /**
     * ALL given flags are present
     * */
    @Deprecated("")
    fun hasFlagsAll(vararg flags: FLAG) =
        flags.firstOrNull { flag -> noFlag(flag) } ?.let { false } ?: true
    /**
     * NONE of given flags are present
     * */
    @Deprecated("")
    fun noFlagsNone(vararg flags: FLAG) =
        flags.firstOrNull { flag -> hasFlag(flag) } ?.let { false } ?: true


//    var disabledFlags: Int

//    fun allowFlag(flag0: IFlagEnum) = disabledFlags and flag0.value == 0 && enabledFlags and flag0.value > 0
//    fun disableFlag(flag0: IFlagEnum) {
//        disabledFlags = disabledFlags or flag0.value
//    }
//    fun disableFlag(flags: Int) {
//        disabledFlags = disabledFlags or flags
//    }

    /**
     *
     *  */
    interface IFlagEnum {
        val mask: Int
    }
}
