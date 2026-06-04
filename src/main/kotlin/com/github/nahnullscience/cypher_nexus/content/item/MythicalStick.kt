package com.github.nahnullscience.cypher_nexus.content.item

import com.github.nahnullscience.cypher_nexus.init.ModDataComponents
import com.github.nahnullscience.cypher_nexus.mechanic.wand.AbstractItemWand
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.WandDataHighPayload
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.WandDataInvariable
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.WandDataInvariable.Companion.TEST_GOOD_WAND
import com.github.nahnullscience.cypher_nexus.utility.mod.ArrayOfCyphers

object MythicalStick: AbstractItemWand(
    Properties()
        .component(ModDataComponents.WAND_INVARIABLE, TEST_GOOD_WAND)
        .component(ModDataComponents.WAND_HIGH_PAYLOAD, WandDataHighPayload(ArrayOfCyphers(26)))
) {
    override val isEditableWand = true
}