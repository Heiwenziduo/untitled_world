package com.github.nahnullscience.cypher_nexus.datagen.provider

import com.github.nahnullscience.cypher_nexus.CypherNexus
import net.minecraft.data.PackOutput
import net.neoforged.neoforge.client.model.generators.BlockModelProvider
import net.neoforged.neoforge.common.data.ExistingFileHelper

class ClientBlockModelProvider(output: PackOutput, existingFileHelper: ExistingFileHelper) :
    BlockModelProvider(output, CypherNexus.MOD_ID, existingFileHelper
) {
    override fun registerModels() {

    }
}