package com.github.nahnullscience.cypher_nexus.network

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload

/**
 * server -> client
 * sync modifier-list inside the cypher-projectile
 * */
@Deprecated("better implementation is to register an EntityDataSerializer, which also handle the sync timing")
data class CypherEntitySyncData(
    val projectileCypher: AbstractCypher,
    val invokeList: List<AbstractCypher>
) : CustomPacketPayload {

    override fun type() = TYPE

    companion object {
        val TYPE: CustomPacketPayload.Type<CypherEntitySyncData> =
            CustomPacketPayload.Type(CypherNexus.modResource("cypher_entity_data"))

        val STREAM: StreamCodec<RegistryFriendlyByteBuf, CypherEntitySyncData> = StreamCodec.composite(
            CNCodecs.CYPHER_STREAM, CypherEntitySyncData::projectileCypher,
            CNCodecs.CYPHER_LIST_STREAM, CypherEntitySyncData::invokeList,
            ::CypherEntitySyncData
        )
    }
}