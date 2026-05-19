package com.github.nahnullscience.cypher_nexus.client.cypher.visualizer

import com.github.nahnullscience.cypher_nexus.client.cypher.ICypherVisualizer
import com.github.nahnullscience.cypher_nexus.content.cypher.projectile.SpawnEggCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

object SpawnEggVi : ICypherVisualizer.IBasicItemVisualizer {
    override fun cypher(): AbstractCypher = SpawnEggCypher
    override val stack = ItemStack(Items.EGG)
}