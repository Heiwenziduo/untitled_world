package com.github.nahnullscience.cypher_nexus.network.server

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.network.client.ClientboundEditWandCyphersConfirm
import com.github.nahnullscience.cypher_nexus.utility.mod.ArrayOfCyphers
import com.github.nahnullscience.cypher_nexus.utility.mod.CNCodecs
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload

data class ServerboundEditWandCyphers(
    val uuid: String,
    val cyphers : List<AbstractCypher> // TODO use AOC instead
) : CustomPacketPayload {
    override fun type() = TYPE

    fun confirm() = ClientboundEditWandCyphersConfirm(uuid, ArrayOfCyphers(cyphers))

    companion object {
        val TYPE: CustomPacketPayload.Type<ServerboundEditWandCyphers> =
            CustomPacketPayload.Type(CypherNexus.modResource("edit_wand_cyphers"))

        val STREAM: StreamCodec<RegistryFriendlyByteBuf, ServerboundEditWandCyphers> = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ServerboundEditWandCyphers::uuid,
            CNCodecs.CYPHER_LIST_STREAM, ServerboundEditWandCyphers::cyphers,
            ::ServerboundEditWandCyphers
        )
    }
}