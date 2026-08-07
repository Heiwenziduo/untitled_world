package com.github.nahnullscience.cypher_nexus.content.item

import com.github.nahnullscience.cypher_nexus.mechanic.wand.AbstractItemWand
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.inventory.tooltip.TooltipComponent
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.ItemUseAnimation
import java.util.Optional

/**
 *
 * */
open class TieredWandItem(
    property: Item.Properties
) : AbstractItemWand(
    property
) {
    override val isEditableWand = true

    override fun getUseAnimation(stack: ItemStack): ItemUseAnimation = ItemUseAnimation.SPEAR



    override fun getDefaultInstance(): ItemStack {
        return super.getDefaultInstance()
    }



    override fun getName(itemStack: ItemStack): Component {
//        val uu = getWandData(itemStack)?.invariable?.uuid.toString()
        return Component.empty()
            .append(CommonComponents.TRANSFER_CONNECT_FAILED)
            .append(CommonComponents.NEW_LINE)
            .append(CommonComponents.GUI_DISCONNECT)
            .append(CommonComponents.SPACE).append(CommonComponents.CONNECT_FAILED)
//            .append(uu)
    }

    override fun getTooltipImage(itemStack: ItemStack): Optional<TooltipComponent> {
        return super.getTooltipImage(itemStack)
    }
}