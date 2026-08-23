package com.github.nahnullscience.cypher_nexus.network.server

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.ModDataAttachments.WAND_INSTANCE_MAP
import com.github.nahnullscience.cypher_nexus.init.ModDataComponents
import com.github.nahnullscience.cypher_nexus.mechanic.event.CNCommonEvents
import com.github.nahnullscience.cypher_nexus.mechanic.wand.IItemWand
import com.github.nahnullscience.cypher_nexus.mechanic.wand.IItemWand.Companion.editRecipeIfWand
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
                var stack: ItemStack? = null

                run {
                    CNCommonEvents.livingGatherWandsTracking(player) { index, wand ->
                        val inv = wand.get(ModDataComponents.WAND_INVARIABLE)
                        if (inv != null && inv.uuid == data.uuid) {
                            stack = wand
                            return@run
                        }
                    }
                }

                // TODO check data authentic
                stack?.editRecipeIfWand(data.cyphers)?.let {
                    if (it) {
                        player.getData(WAND_INSTANCE_MAP).updateWandInstance(player.level(), stack, stack.item as IItemWand)
                        PacketDistributor.sendToPlayer(player as ServerPlayer, data.makeConfirm())
                    }
                } ?: CypherNexus.debugWand(Level.ERROR) { "wand didn't find [${data.uuid}]" }

            }.exceptionally {
                CypherNexus.debugNetwork(Level.WARN) { it.message.toString() }
                return@exceptionally null
            }
        }
    }
}
