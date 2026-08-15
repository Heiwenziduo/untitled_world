package com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking

enum class InvokingResult(
    val success: Boolean,
) {
    MISSING_DATA(false),
    MISSING_INSTANCE(false),
    LOADING(false),

    /**
     * waiting for threaded result
     * */
    HANG(true),
    SUCCESS(true),
}