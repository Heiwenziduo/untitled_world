package com.github.nahnullscience.cypher_nexus.utility

object CNUtils {

    fun stepAdvanceEvery2(check: Int): Boolean {
        return check and 1 > 0
    }

    fun stepAdvanceEvery4(check: Int): Boolean {
        return check and 2 > 0 && stepAdvanceEvery2(check)
    }

    fun stepAdvanceEvery8(check: Int): Boolean {
        return check and 4 > 0 && stepAdvanceEvery4(check)
    }

    fun stepAdvanceEvery16(check: Int): Boolean {
        return check and 8 > 0 && stepAdvanceEvery8(check)
    }

    fun stepAdvanceEvery32(check: Int): Boolean {
        return check and 16 > 0 && stepAdvanceEvery16(check)
    }

    fun stepAdvanceEvery64(check: Int): Boolean {
        return check and 32 > 0 && stepAdvanceEvery32(check)
    }

    fun stepAdvanceEvery128(check: Int): Boolean {
        return check and 64 > 0 && stepAdvanceEvery64(check)
    }

    fun stepAdvanceEvery256(check: Int): Boolean {
        return check and 128 > 0 && stepAdvanceEvery128(check)
    }

    // this make sense?
}