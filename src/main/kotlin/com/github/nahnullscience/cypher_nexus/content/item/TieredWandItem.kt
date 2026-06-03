package com.github.nahnullscience.cypher_nexus.content.item

import com.github.nahnullscience.cypher_nexus.init.ModDataComponents
import com.github.nahnullscience.cypher_nexus.mechanic.wand.AbstractItemWand
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.WandDataHighPayload
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.WandDataInvariable

/**
 *
 * */
open class TieredWandItem() : AbstractItemWand(
    Properties()
        .component(ModDataComponents.WAND_INVARIABLE, WandDataInvariable.Companion.DEFAULT)
        .component(ModDataComponents.WAND_HIGH_PAYLOAD, WandDataHighPayload.Companion.DEFAULT)
) {
    override val isEditableWand = true
}