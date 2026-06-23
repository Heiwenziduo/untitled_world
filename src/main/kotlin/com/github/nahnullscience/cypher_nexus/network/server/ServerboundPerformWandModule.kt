package com.github.nahnullscience.cypher_nexus.network.server

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.ModuleCategory
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload

data class ServerboundPerformWandModule(
    val uuid: String,
    val module: ModuleCategory,
    val wandSlot: Int
) : CustomPacketPayload {
    override fun type() = TYPE

    companion object {
        val TYPE: CustomPacketPayload.Type<ServerboundPerformWandModule> =
            CustomPacketPayload.Type(CypherNexus.modResource("perform_wand_module"))

        val STREAM: StreamCodec<RegistryFriendlyByteBuf, ServerboundPerformWandModule> = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ServerboundPerformWandModule::uuid,
            ModuleCategory.STREAM_CODEC, ServerboundPerformWandModule::module,
            ByteBufCodecs.VAR_INT, ServerboundPerformWandModule::wandSlot,
            ::ServerboundPerformWandModule
        )
    }
}