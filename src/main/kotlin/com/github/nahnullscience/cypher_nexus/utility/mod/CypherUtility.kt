package com.github.nahnullscience.cypher_nexus.utility.mod

import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategories
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.category.CypherCategory
import com.github.nahnullscience.cypher_nexus.mechanic.wand.IWandLike
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.ItemWandInstance
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity
import kotlin.collections.forEach

object CypherUtility {

    fun sortCyphersByCategory(list: List<AbstractCypher>): Map<CypherCategory, List<AbstractCypher>> {
        val map = mutableMapOf<CypherCategory, MutableList<AbstractCypher>>()
        CypherCategories.REGISTRY.toList().forEach { category -> map[category] = mutableListOf() } // this will keep map in category registry order
        list.forEach { cypher ->
            val list0 = map.getValue(cypher.category.value())
            list0.add(cypher)
        }
        return map
    }

    fun LivingEntity.getWandInHand(hand: InteractionHand) : IWandLike? {
        val wand = this.getItemInHand(hand)
        return if (IWandLike.validateItemWand(wand)) {
            wand.item as IWandLike
        } else null
    }

    fun LivingEntity.getHandWandInstance(hand: InteractionHand) : ItemWandInstance? {
        return getWandInHand(hand)?.itemWandInstance(level(), this, getItemInHand(hand))
    }
}