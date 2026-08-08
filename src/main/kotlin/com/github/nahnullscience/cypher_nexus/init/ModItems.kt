package com.github.nahnullscience.cypher_nexus.init

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.content.item.MythicalStick
import com.github.nahnullscience.cypher_nexus.content.item.TieredWandItem
import com.github.nahnullscience.cypher_nexus.mechanic.wand.AbstractItemWand
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.WandDataHighPayload
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.ItemWandDataInvariable
import net.minecraft.world.item.BlockItem
import net.neoforged.neoforge.registries.DeferredRegister
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.neoforge.forge.getValue

object ModItems {
    val DEFERRED_REGISTER: DeferredRegister.Items = DeferredRegister.createItems(CypherNexus.MOD_ID)

    fun register() {
        DEFERRED_REGISTER.register(MOD_BUS)
    }

    val MYTHICAL_STICK: AbstractItemWand by DEFERRED_REGISTER.registerItem("mythical_stick", ::MythicalStick)
    { properties -> properties
        .component(ModDataComponents.WAND_INVARIABLE, ItemWandDataInvariable.good())
        .component(ModDataComponents.WAND_HIGH_PAYLOAD, WandDataHighPayload.of(63))
    }

    val TIERED_WAND: AbstractItemWand by DEFERRED_REGISTER.registerItem("tiered_wand", ::TieredWandItem)
    { properties -> properties
        .component(ModDataComponents.WAND_INVARIABLE, ItemWandDataInvariable.TO_BE_GENERATED)
    }

    // When it comes to mass, guess I can make a factory function to auto register block-item.
    val CYPHER_INDEX_BLOCK_ITEM: BlockItem by DEFERRED_REGISTER.registerSimpleBlockItem("cypher_index") { ->
        ModBlocks.CYPHER_INDEX_BLOCK
    }
}