package com.github.nahnullscience.cypher_nexus.network.server

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.mod.WandModuleTypes.RESOURCE_KEY
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.LivingModuleCommon
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.WandModuleType
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.neoforged.neoforge.network.handling.IPayloadContext
import org.apache.logging.log4j.Level

data class ServerboundWandModuleEnd(
    val module: WandModuleType<*>
) : CustomPacketPayload {
    override fun type() = TYPE

    companion object {
        val TYPE: CustomPacketPayload.Type<ServerboundWandModuleEnd> =
            CustomPacketPayload.Type(CypherNexus.modResource("perform_module_end"))

        val STREAM: StreamCodec<RegistryFriendlyByteBuf, ServerboundWandModuleEnd> = StreamCodec.composite(
            ByteBufCodecs.registry(RESOURCE_KEY), ServerboundWandModuleEnd::module,
            ::ServerboundWandModuleEnd
        )

        fun handler(data: ServerboundWandModuleEnd, context: IPayloadContext) {
            CypherNexus.debugNetwork { "server receive package -> performWandModuleEnd: \n$data" }

            context.enqueueWork {
                val player = context.player()
                LivingModuleCommon.endIfPerformingThen(player, data.module) {

                }

            }.exceptionally {
                CypherNexus.debugNetwork(Level.WARN) { it.message.toString() }
                return@exceptionally null
            }
        }
    }
}