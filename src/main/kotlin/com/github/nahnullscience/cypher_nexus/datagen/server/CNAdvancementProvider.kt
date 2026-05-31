package com.github.nahnullscience.cypher_nexus.datagen.server

import com.github.nahnullscience.cypher_nexus.datagen.server.generator.ServerAdvancementGenerator
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.neoforged.neoforge.common.data.AdvancementProvider
import net.neoforged.neoforge.common.data.ExistingFileHelper
import java.util.concurrent.CompletableFuture

class CNAdvancementProvider(
    output: PackOutput,
    registries: CompletableFuture<HolderLookup.Provider>,
    existingFileHelper: ExistingFileHelper,
) : AdvancementProvider(
    output,
    registries,
    existingFileHelper,
    listOf(
        ServerAdvancementGenerator(),
    )
) {

}