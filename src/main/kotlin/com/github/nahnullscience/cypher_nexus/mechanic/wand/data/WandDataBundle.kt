//package com.github.nahnullscience.cypher_nexus.mechanic.wand.data
//
//import com.github.nahnullscience.cypher_nexus.CypherNexus
//
//data class WandDataBundle(val invariable: ItemWandDataInvariable, val highPayload: WandDataHighPayload) {
//
//    companion object {
//        inline fun missingData(msg: () -> String) = WandDataBundle(
//            ItemWandDataInvariable.FALL_BACK,
//            WandDataHighPayload.EMPTY
//            ).also { CypherNexus.debugWand(supplier = msg) }
//    }
//}