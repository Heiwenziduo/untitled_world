package com.github.nahnullscience.cypher_nexus.init

import com.github.nahnullscience.cypher_nexus.CypherNexus
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.CreativeModeTab
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS

object ModTabs {
    val DEFERRED_REGISTER: DeferredRegister<CreativeModeTab> =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CypherNexus.MOD_ID)

    fun register() {
        DEFERRED_REGISTER.register(MOD_BUS)
    }

    val BASE_MODE_TAB: DeferredHolder<CreativeModeTab, CreativeModeTab> =
        DEFERRED_REGISTER.register(CypherNexus.MOD_ID, { ->
        CreativeModeTab.builder()
            .title(CypherNexus.modTranslation("item_group"))
            .icon { -> ModItems.TIERED_WAND.defaultInstance }
            .displayItems { parameters, output ->
                // add items while BuildCreativeModeTabContentsEvent is feasible too
                output.accept { ModItems.CYPHER_INDEX_BLOCK_ITEM }

                output.accept { ModItems.MYTHICAL_STICK }
                output.accept { ModItems.TIERED_WAND }
            }
            .build()
    })

}