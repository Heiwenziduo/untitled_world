package com.github.nahnullscience.cypher_nexus.init

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.WandDataHighPayload
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.WandDataInvariable
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.Registries
import net.neoforged.neoforge.registries.DeferredRegister
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS
import java.util.function.Supplier


object ModDataComponents {
    val DEFERRED_REGISTER: DeferredRegister.DataComponents =
        DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, CypherNexus.MOD_ID)

    fun register() {
        DEFERRED_REGISTER.register(MOD_BUS)
    }

    val WAND_INVARIABLE: Supplier<DataComponentType<WandDataInvariable>> =
        DEFERRED_REGISTER.registerComponentType("wand_invariable")
        { it.persistent(WandDataInvariable.Companion.INVARIABLE_DATA_CODEC).networkSynchronized(WandDataInvariable.Companion.INVARIABLE_DATA_STREAM) }

    val WAND_HIGH_PAYLOAD: Supplier<DataComponentType<WandDataHighPayload>> =
        DEFERRED_REGISTER.registerComponentType("wand_high_payload")
        { it.persistent(WandDataHighPayload.Companion.HIGH_PAYLOAD_DATA_CODEC).networkSynchronized(WandDataHighPayload.Companion.HIGH_PAYLOAD_DATA_STREAM) }

}