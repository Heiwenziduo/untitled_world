package com.github.nahnullscience.cypher_nexus.network.client

import com.github.nahnullscience.cypher_nexus.CypherNexus
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload

data class ClientboundSyncWandInstance(
    val uuid: String,
    val mana: Float,
    val delay: Int,
    val recharge: Int,
    val deck: Long
) : CustomPacketPayload {
    override fun type() = TYPE

    companion object {
        val TYPE: CustomPacketPayload.Type<ClientboundSyncWandInstance> =
            CustomPacketPayload.Type(CypherNexus.modResource("sync_wand_instance"))

        val STREAM: StreamCodec<ByteBuf, ClientboundSyncWandInstance> = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ClientboundSyncWandInstance::uuid,
            ByteBufCodecs.FLOAT, ClientboundSyncWandInstance::mana,
            ByteBufCodecs.INT, ClientboundSyncWandInstance::delay,
            ByteBufCodecs.INT, ClientboundSyncWandInstance::recharge,
            ByteBufCodecs.VAR_LONG, ClientboundSyncWandInstance::deck,
            ::ClientboundSyncWandInstance
        )
    }
}