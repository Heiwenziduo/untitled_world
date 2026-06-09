package com.github.nahnullscience.cypher_nexus.datagen

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.datagen.client.CNModelProvider
import com.github.nahnullscience.cypher_nexus.datagen.server.CNAdvancementProviders
import com.github.nahnullscience.cypher_nexus.datagen.server.CNDataMapProvider
import com.github.nahnullscience.cypher_nexus.datagen.server.CNLootTableProviders
import net.minecraft.data.advancements.AdvancementProvider
import net.minecraft.data.loot.LootTableProvider
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.data.event.GatherDataEvent


/**
 * an object singleton that handle data-relevant events
 * */
@EventBusSubscriber(modid = CypherNexus.MOD_ID)
object ModData {

    // on the mod event bus
    @SubscribeEvent
    fun gatherData(event: GatherDataEvent.Client) {
        // DataGenerator, that we register the providers to.
        val generator = event.generator
        // PackOutput, used by some providers to determine their file output location.
        val output = generator.packOutput
        // CompletableFuture<HolderLookup.Provider>,
        // mainly used by tags and datagen registries to reference other, potentially not yet existing elements.
        val lookupProvider = event.lookupProvider

        // client
        event.createProvider(::CNModelProvider)

        // server
        event.createProvider(::CNDataMapProvider)
        event.createProvider { output, lookupProvider ->
            AdvancementProvider(output, lookupProvider, listOf(
                CNAdvancementProviders.GenerateCypherIndex)) }
        event.createProvider { output, lookupProvider ->
            LootTableProvider(output, setOf(),
                CNLootTableProviders.providers, lookupProvider) }
    }

    @SubscribeEvent
    fun gatherData(event: GatherDataEvent.Server) {
        /* @doc
         * on higher versions (1.21.11) GatherDataEvent.Client & GatherDataEvent.Server are independent,
         * generating them by running the runClientData and runServerData tasks, respectively.
         * */
        /* @doc
         * There are two recommendations on how to register your providers.
         * The former is to register all of them in GatherDataEvent.Client and use the runClientData task to generate the data.
         * The latter is to register client providers to GatherDataEvent.Client and server providers to
         * GatherDataEvent.Server, generating them by running the runClientData and runServerData tasks, respectively.
         * */
    }
}