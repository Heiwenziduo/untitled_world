package com.github.nahnullscience.cypher_nexus.utility.i

/**
 * utilities to store, use, computed bit-integers
 * */
interface IFlagExtension {

    /**
     * a flag is basically a bundle of booleans
     * */
    var enabledFlags: Int
//    var disabledFlags: Int

//    fun allowFlag(flag0: IFlagEnum) = disabledFlags and flag0.value == 0 && enabledFlags and flag0.value > 0
    fun haveFlag(flag0: IFlagEnum) = enabledFlags and flag0.value == flag0.value
    /**
     * ALL given flags are present
     * */
    fun haveFlagsAll(vararg flags: IFlagEnum) =
        flags.firstOrNull { flag -> noFlag(flag) } ?.let { false } ?: true

    fun noFlag(flag0: IFlagEnum) = !haveFlag(flag0)
    /**
     * NONE of given flags are present
     * */
    fun noFlagsNone(vararg flags: IFlagEnum) =
        flags.firstOrNull { flag -> haveFlag(flag) } ?.let { false } ?: true

    fun enableFlag(flag0: IFlagEnum) = let { enabledFlags = enabledFlags or flag0.value }
    fun enableFlag(flags: Int) = let { enabledFlags = enabledFlags or flags }

//    fun disableFlag(flag0: IFlagEnum) {
//        disabledFlags = disabledFlags or flag0.value
//    }
//    fun disableFlag(flags: Int) {
//        disabledFlags = disabledFlags or flags
//    }


    interface IFlagEnum {
        val value: Int
    }
}