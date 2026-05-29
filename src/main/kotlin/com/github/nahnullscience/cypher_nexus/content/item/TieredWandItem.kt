package com.github.nahnullscience.cypher_nexus.content.item

import com.github.nahnullscience.cypher_nexus.init.ModDataComponents
import com.github.nahnullscience.cypher_nexus.mechanic.wand.AbstractItemWand
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.WandDataFrequent
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.WandDataHighPayload
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.WandDataInvariable
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.UseAnim

/**
 *
 * */
open class TieredWandItem() : AbstractItemWand(
    Properties()
        .component(ModDataComponents.WAND_INVARIABLE, WandDataInvariable.Companion.DEFAULT)
        .component(ModDataComponents.WAND_HIGH_PAYLOAD, WandDataHighPayload.Companion.DEFAULT)
        .component(ModDataComponents.WAND_FREQUENT, WandDataFrequent.Companion.DEFAULT)
) {
    override val isEditableWand = true
    override fun getUseAnimation(stack: ItemStack): UseAnim {
        return UseAnim.BOW
    }
}