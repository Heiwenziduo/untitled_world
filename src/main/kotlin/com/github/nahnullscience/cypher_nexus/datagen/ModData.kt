package com.github.nahnullscience.cypher_nexus.datagen

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.datagen.client.CNBlockStateProvider
import com.github.nahnullscience.cypher_nexus.datagen.client.CNItemModelProvider
import com.github.nahnullscience.cypher_nexus.datagen.loot_table.CNGlobalLootModifierProvider
import com.github.nahnullscience.cypher_nexus.datagen.loot_table.CNLootTableProvider
import com.github.nahnullscience.cypher_nexus.datagen.server.CNAdvancementProvider
import com.github.nahnullscience.cypher_nexus.datagen.server.CNDataMapProvider
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.data.event.GatherDataEvent


/**
 * an object singleton that handle data-relevant events
 * */
@EventBusSubscriber(modid = CypherNexus.MOD_ID)
object ModData {

    /* @doc
     * on higher versions (1.21.11) GatherDataEvent.Client & GatherDataEvent.Server are independent,
     * generating them by running the runClientData and runServerData tasks, respectively.
     * */
    // on the mod event bus
    @SubscribeEvent
    fun gatherData(event: GatherDataEvent) {
        // DataGenerator, that we register the providers to.
        val generator = event.generator
        // PackOutput, used by some providers to determine their file output location.
        val output = generator.packOutput
        // ExistingFileHelper, used by providers for things that can reference other files.
        val existingFileHelper = event.existingFileHelper
        // CompletableFuture<HolderLookup.Provider>,
        // mainly used by tags and datagen registries to reference other, potentially not yet existing elements.
        val lookupProvider = event.lookupProvider

        val server = event.includeServer()
        val client = event.includeClient()

        // assets on the client
        generator.addProvider(client, CNItemModelProvider(output, existingFileHelper))
        generator.addProvider(client, CNBlockStateProvider(output, existingFileHelper))
        // and data on the server
        generator.addProvider(server, CNAdvancementProvider(output, lookupProvider, existingFileHelper))
        generator.addProvider(server, CNGlobalLootModifierProvider(output, lookupProvider))
        generator.addProvider(server, CNLootTableProvider(output, lookupProvider))
        generator.addProvider(server, CNDataMapProvider(output, lookupProvider))
    }
}