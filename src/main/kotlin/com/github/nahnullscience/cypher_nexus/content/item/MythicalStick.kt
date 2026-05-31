package com.github.nahnullscience.cypher_nexus.content.item

import com.github.nahnullscience.cypher_nexus.init.ModDataComponents
import com.github.nahnullscience.cypher_nexus.mechanic.wand.AbstractItemWand
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.WandDataFrequent
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.WandDataHighPayload
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.WandDataInvariable
import com.github.nahnullscience.cypher_nexus.utility.mod.ArrayOfCyphers

object MythicalStick: AbstractItemWand(
    Properties()
        .component(
            ModDataComponents.WAND_INVARIABLE,
            WandDataInvariable(
                chunkF = WandDataInvariable.WandDataChunkF(3000f, 30f, 1.6f),
                chunkI = WandDataInvariable.WandDataChunkI(20, 1, 6, 10),
                chunkL = WandDataInvariable.WandDataChunkL(listOf()),
                chunkU = WandDataInvariable.WandDataChunkU("test_wand")
            )
        )
        .component(ModDataComponents.WAND_HIGH_PAYLOAD, WandDataHighPayload(ArrayOfCyphers(20)))
        .component(ModDataComponents.WAND_FREQUENT, WandDataFrequent.Companion.DEFAULT)
) {
    override val isEditableWand = true
}