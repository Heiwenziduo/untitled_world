package com.github.nahnullscience.cypher_nexus.mechanic.wand.data

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher

interface IWandData {

    val chunkF: WandDataChunkF
    val chunkI: WandDataChunkI

    data class WandDataChunkF(val manaMax: Float, val manaRegen: Float, val spread: Float)
    data class WandDataChunkI(val draw: Int, val castDelay: Int, val rechargeTime: Int,)
    data class WandDataChunkL(val alwaysInvoke: List<AbstractCypher>)
}