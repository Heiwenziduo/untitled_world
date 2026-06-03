package com.github.nahnullscience.cypher_nexus.datagen.server

import com.github.nahnullscience.cypher_nexus.init.ModDataMaps
import com.github.nahnullscience.cypher_nexus.init.mod.Cyphers
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.neoforged.neoforge.common.data.DataMapProvider
import java.util.concurrent.CompletableFuture

class CNDataMapProvider(
    packOutput: PackOutput,
    lookupProvider: CompletableFuture<HolderLookup.Provider>
) : DataMapProvider(packOutput, lookupProvider) {
    override fun gather(provider: HolderLookup.Provider) {
        val builder = builder(ModDataMaps.CYPHER_DATA_ATTACH).replace(false)

//        val cypherLookup = provider.lookupOrThrow(Cyphers.RESOURCE_KEY)
//        cypherLookup.listElements().forEach { reference ->
//            builder.add(reference.key(), reference.value().defaultAttributes().build(), false)
//        }

        for (i in Cyphers.REGISTRY.holders()) {
            builder.add(i, i.value().defaultAttributes().build(), false)
        }
    }
}