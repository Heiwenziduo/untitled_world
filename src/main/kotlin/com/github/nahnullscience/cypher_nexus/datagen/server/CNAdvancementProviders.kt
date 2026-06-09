package com.github.nahnullscience.cypher_nexus.datagen.server

import net.minecraft.advancements.AdvancementHolder
import net.minecraft.core.HolderLookup
import net.minecraft.data.advancements.AdvancementSubProvider
import java.util.function.Consumer

object CNAdvancementProviders {

//    fun generateCypherIndex(registries: HolderLookup.Provider, saver: Consumer<AdvancementHolder>) {
//
//    }
    object GenerateCypherIndex : AdvancementSubProvider {
        override fun generate(
            registries: HolderLookup.Provider,
            output: Consumer<AdvancementHolder>
        ) {

        }
    }

}