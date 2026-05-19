package com.github.nahnullscience.cypher_nexus.network.client

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.network.CNCodecs
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload

data class ClientboundOpenIndexScreen(
    val cyphersTotal: List<AbstractCypher>,
    val cyphersUnlocked: List<AbstractCypher>,
) : CustomPacketPayload {
    override fun type() = TYPE

    companion object {
        val TYPE: CustomPacketPayload.Type<ClientboundOpenIndexScreen> =
            CustomPacketPayload.Type(CypherNexus.modResource("open_index_screen"))

        val STREAM: StreamCodec<RegistryFriendlyByteBuf, ClientboundOpenIndexScreen> = StreamCodec.composite(
            CNCodecs.CYPHER_LIST_STREAM, ClientboundOpenIndexScreen::cyphersTotal,
            CNCodecs.CYPHER_LIST_STREAM, ClientboundOpenIndexScreen::cyphersUnlocked,
            ::ClientboundOpenIndexScreen
        )
    }
}