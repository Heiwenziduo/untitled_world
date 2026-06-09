package com.github.nahnullscience.cypher_nexus.content.item

import com.github.nahnullscience.cypher_nexus.mechanic.wand.AbstractItemWand
import net.minecraft.world.item.Item

/**
 *
 * */
open class TieredWandItem(
    property: Item.Properties
) : AbstractItemWand(
    property
) {
    override val isEditableWand = true
}