package com.github.nahnullscience.cypher_nexus.init

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.content.item.AbstractItemWand
import com.github.nahnullscience.cypher_nexus.content.item.MythicalStick
import com.github.nahnullscience.cypher_nexus.content.item.TieredWandItem
import net.minecraft.world.item.BlockItem
import net.neoforged.neoforge.registries.DeferredRegister
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.neoforge.forge.getValue

object ModItems {
    val DEFERRED_REGISTER: DeferredRegister.Items = DeferredRegister.createItems(CypherNexus.MOD_ID)

    fun register() {
        DEFERRED_REGISTER.register(MOD_BUS)
    }

    val MYTHICAL_STICK: AbstractItemWand by DEFERRED_REGISTER.register("mythical_stick") { registryName -> MythicalStick }
    val TIERED_WAND: AbstractItemWand by DEFERRED_REGISTER.register("basic_wand") { registryName -> TieredWandItem() }

    // When it comes to mass, guess I can make a factory function to auto register block-item.
    val CYPHER_INDEX_BLOCK_ITEM: BlockItem by DEFERRED_REGISTER.registerSimpleBlockItem("cypher_index") { ->
        ModBlocks.CYPHER_INDEX_BLOCK
    }
}