package com.github.nahnullscience.cypher_nexus.content.item

import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.UseAnim

/**
 *
 * */
open class TieredWandItem() : AbstractItemWand() {
    override val isEditableWand = true
    override fun getUseAnimation(stack: ItemStack): UseAnim {
        return UseAnim.BOW
    }
}