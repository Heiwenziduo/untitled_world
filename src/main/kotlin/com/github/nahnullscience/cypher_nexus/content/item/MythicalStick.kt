package com.github.nahnullscience.cypher_nexus.content.item

import com.github.nahnullscience.cypher_nexus.mechanic.wand.AbstractItemWand
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

class MythicalStick(
    property: Item.Properties
): AbstractItemWand(
    property
) {
    override val isEditableWand = true
    override fun generateWandData(stack: ItemStack) {

    }
}