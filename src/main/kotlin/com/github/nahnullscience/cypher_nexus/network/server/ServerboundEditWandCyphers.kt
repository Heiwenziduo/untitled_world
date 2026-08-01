package com.github.nahnullscience.cypher_nexus.network.server

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.ModDataAttachments.WAND_DATA_MAP
import com.github.nahnullscience.cypher_nexus.init.ModDataComponents
import com.github.nahnullscience.cypher_nexus.mechanic.event.CNCommonEvents
import com.github.nahnullscience.cypher_nexus.mechanic.wand.IWandLike
import com.github.nahnullscience.cypher_nexus.mechanic.wand.IWandLike.Companion.editRecipeIfWand
import com.github.nahnullscience.cypher_nexus.network.client.ClientboundEditWandCyphersConfirm
import com.github.nahnullscience.cypher_nexus.utility.mod.ArrayOfCyphers
import com.github.nahnullscience.cypher_nexus.utility.mod.CNCodecs
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.network.PacketDistributor
import net.neoforged.neoforge.network.handling.IPayloadContext
import org.apache.logging.log4j.Level
import java.util.*

data class ServerboundEditWandCyphers(
    val uuid: UUID,
    val cyphers : ArrayOfCyphers
) : CustomPacketPayload {

    override fun type() = TYPE

    fun makeConfirm() = ClientboundEditWandCyphersConfirm(uuid, cyphers)

    companion object {
        val TYPE: CustomPacketPayload.Type<ServerboundEditWandCyphers> =
            CustomPacketPayload.Type(CypherNexus.modResource("edit_wand_cyphers"))

        val STREAM: StreamCodec<RegistryFriendlyByteBuf, ServerboundEditWandCyphers> = StreamCodec.composite(
            CNCodecs.UUID_STREAM, ServerboundEditWandCyphers::uuid,
            CNCodecs.AOC_STREAM, ServerboundEditWandCyphers::cyphers,
            ::ServerboundEditWandCyphers
        )

        fun handler(data: ServerboundEditWandCyphers, context: IPayloadContext) {
            CypherNexus.debugNetwork { "server receive package -> editWandCyphers: \n$data" }

            context.enqueueWork {
                val player = context.player()
                var stack: ItemStack

                val w = CNCommonEvents.livingGatherWandsTracking(player).wands().filter { stack ->
                    val i = stack.get(ModDataComponents.WAND_INVARIABLE)
                    i != null && i.uuid == data.uuid
                }

                if (w.size > 1) CypherNexus.debugWand(Level.ERROR) { "duplicate uuid [${data.uuid}] $w" }
                stack = w.first()

                // TODO check data authentic
                stack.editRecipeIfWand(data.cyphers).let {
                    if (it) {
                        player.getData(WAND_DATA_MAP).updateWandStats(stack, stack.item as IWandLike, player.level())
                        PacketDistributor.sendToPlayer(player as ServerPlayer, data.makeConfirm())
                    }
                }
            }.exceptionally {
                CypherNexus.debugNetwork(Level.WARN) { it.message.toString() }
                return@exceptionally null
            }
        }
    }
}