package com.github.nahnullscience.cypher_nexus.datagen.client

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.ModBlocks
import com.github.nahnullscience.cypher_nexus.init.ModItems
import net.minecraft.client.data.models.BlockModelGenerators
import net.minecraft.client.data.models.ItemModelGenerators
import net.minecraft.client.data.models.ModelProvider
import net.minecraft.client.data.models.model.ModelTemplates
import net.minecraft.data.PackOutput

class CNModelProvider(output: PackOutput) : ModelProvider(output, CypherNexus.MOD_ID) {

    override fun registerModels(blockModels: BlockModelGenerators, itemModels: ItemModelGenerators) {
        // no super call here, which performs vanilla logic and will interfere with mod resource
        block(blockModels)
        item(itemModels)
    }

    private fun block(blockModels: BlockModelGenerators) {
        blockModels.createTrivialCube(ModBlocks.CYPHER_INDEX_BLOCK)
    }

    private fun item(itemModels: ItemModelGenerators) {
        itemModels.generateFlatItem(ModItems.MYTHICAL_STICK, ModelTemplates.FLAT_HANDHELD_ROD_ITEM)
        itemModels.generateFlatItem(ModItems.TIERED_WAND, ModelTemplates.FLAT_HANDHELD_ROD_ITEM)
    }
}