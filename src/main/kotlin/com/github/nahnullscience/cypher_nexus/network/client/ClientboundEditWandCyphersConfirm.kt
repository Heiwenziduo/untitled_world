package com.github.nahnullscience.cypher_nexus.network.client

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.utility.mod.ArrayOfCyphers
import com.github.nahnullscience.cypher_nexus.utility.mod.CNCodecs
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload

data class ClientboundEditWandCyphersConfirm(
    val uuid: String,
    val cyphers : ArrayOfCyphers
) : CustomPacketPayload {
    override fun type() = TYPE

    companion object {
        val TYPE: CustomPacketPayload.Type<ClientboundEditWandCyphersConfirm> =
            CustomPacketPayload.Type(CypherNexus.modResource("edit_wand_cyphers_confirm"))

        val STREAM: StreamCodec<RegistryFriendlyByteBuf, ClientboundEditWandCyphersConfirm> = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ClientboundEditWandCyphersConfirm::uuid,
            CNCodecs.AOC_STREAM, ClientboundEditWandCyphersConfirm::cyphers,
            ::ClientboundEditWandCyphersConfirm
        )
    }
}