package com.github.nahnullscience.cypher_nexus.datagen.client

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.ModItems
import net.minecraft.data.PackOutput
import net.neoforged.neoforge.client.model.generators.ItemModelProvider
import net.neoforged.neoforge.common.data.ExistingFileHelper

// item models don't have an equivalent to blockstate files and are instead used directly
class CNItemModelProvider(
    output: PackOutput,
    existingFileHelper: ExistingFileHelper
) : ItemModelProvider(
    output,
    CypherNexus.MOD_ID,
    existingFileHelper
) {
    override fun registerModels() {
        // withExistingParent(ModItems.BASIC_WAND.toString(), mcLoc("item/handheld"))
        //     .texture("layer0", "item/basic_wand")
        // do the same things as the expression above
//        basicItem(ModItems.BASIC_WAND) // "item/generated"
        handheldItem(ModItems.TIERED_WAND) // "item/handheld"
    }
}