package com.github.nahnullscience.cypher_nexus.network.server

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.mod.WandModuleTypes.RESOURCE_KEY
import com.github.nahnullscience.cypher_nexus.mechanic.entity.WandModuleStateTracker.Companion.isPerformingModule
import com.github.nahnullscience.cypher_nexus.mechanic.entity.WandModuleStateTracker.Companion.setPerformingModule
import com.github.nahnullscience.cypher_nexus.mechanic.event.CNCommonEvents
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.WandModuleType
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.neoforged.neoforge.network.handling.IPayloadContext
import org.apache.logging.log4j.Level

data class ServerboundPerformModuleEnd(
    val module: WandModuleType<*>
) : CustomPacketPayload {
    override fun type() = TYPE

    companion object {
        val TYPE: CustomPacketPayload.Type<ServerboundPerformModuleEnd> =
            CustomPacketPayload.Type(CypherNexus.modResource("perform_module_end"))

        val STREAM: StreamCodec<RegistryFriendlyByteBuf, ServerboundPerformModuleEnd> = StreamCodec.composite(
            ByteBufCodecs.registry(RESOURCE_KEY), ServerboundPerformModuleEnd::module,
            ::ServerboundPerformModuleEnd
        )

        fun handler(data: ServerboundPerformModuleEnd, context: IPayloadContext) {
            CypherNexus.debugNetwork { "server receive package -> performWandModuleEnd: \n$data" }

            context.enqueueWork {
                val player = context.player()
                if (player.isPerformingModule(data.module)) {
                    val f = CNCommonEvents.wandModuleEnd(data.module, player.level(), player, null)
                    if (f) player.setPerformingModule(data.module, false)
                } else {

                }

            }.exceptionally {
                CypherNexus.debugNetwork(Level.WARN) { it.message.toString() }
                return@exceptionally null
            }
        }
    }
}