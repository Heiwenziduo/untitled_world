package com.github.nahnullscience.cypher_nexus.network.server

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.ModDataAttachments.WAND_DATA_MAP
import com.github.nahnullscience.cypher_nexus.init.ModDataComponents
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.event.CNCommonEvents
import com.github.nahnullscience.cypher_nexus.mechanic.wand.IWandLike
import com.github.nahnullscience.cypher_nexus.network.client.ClientboundEditWandCyphersConfirm
import com.github.nahnullscience.cypher_nexus.utility.mod.ArrayOfCyphers
import com.github.nahnullscience.cypher_nexus.utility.mod.CNCodecs
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.network.PacketDistributor
import net.neoforged.neoforge.network.handling.IPayloadContext
import org.apache.logging.log4j.Level

data class ServerboundEditWandCyphers(
    val uuid: String,
    val cyphers : List<AbstractCypher>
) : CustomPacketPayload {
    constructor(uuid: String, aoc: ArrayOfCyphers) : this(uuid, aoc.toList())

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

        fun handler(data: ServerboundEditWandCyphers, context: IPayloadContext) {
            CypherNexus.debugNetwork { "server receive package -> editWandCyphers: \n$data" }

            context.enqueueWork {
                val player = context.player()
                var stack: ItemStack = ItemStack.EMPTY

                val w = CNCommonEvents.livingGatherWandsTracking(player).wands().filter { stack ->
                    val i = stack.get(ModDataComponents.WAND_INVARIABLE)
                    i != null && i.uuid == data.uuid }

                if (w.size > 1) CypherNexus.debugWand(Level.ERROR) { "duplicate uuid [${data.uuid}] $w" }
                stack = w.first()

                if (!stack.isEmpty) {
                    // TODO check data authentic
                    IWandLike.editItemWand(stack, data.cyphers)
                    player.getData(WAND_DATA_MAP).updateWandStats(stack, stack.item as IWandLike, player.level())
                    PacketDistributor.sendToPlayer(player as ServerPlayer, data.confirm())
                }

            }.exceptionally {
                CypherNexus.debugNetwork(Level.WARN) { it.message.toString() }
                return@exceptionally null
            }
        }
    }
}