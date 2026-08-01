package com.github.nahnullscience.cypher_nexus.mechanic.wand.data

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.utility.mod.ArrayOfCyphers

data class WandDataBundle(val invariable: WandDataInvariable, val highPayload: WandDataHighPayload) {
    companion object {
        inline fun missingData(msg: () -> String) = WandDataBundle(
            WandDataInvariable.FALL_BACK,
            WandDataHighPayload(ArrayOfCyphers(1))
            ) .also { CypherNexus.debugWand(supplier = msg) }
    }
}