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

data class ServerboundPerformModuleStart(
    val module: WandModuleType<*>
) : CustomPacketPayload {
    override fun type() = TYPE

    companion object {
        val TYPE: CustomPacketPayload.Type<ServerboundPerformModuleStart> =
            CustomPacketPayload.Type(CypherNexus.modResource("perform_module_start"))

        val STREAM: StreamCodec<RegistryFriendlyByteBuf, ServerboundPerformModuleStart> = StreamCodec.composite(
            ByteBufCodecs.registry(RESOURCE_KEY), ServerboundPerformModuleStart::module,
            ::ServerboundPerformModuleStart
        )

        fun handler(data: ServerboundPerformModuleStart, context: IPayloadContext) {
            CypherNexus.debugNetwork { "server receive package -> performWandModuleStart: \n$data" }

            context.enqueueWork {
                val player = context.player()
                if (player.isPerformingModule(data.module)) {

                } else {
                    val t = CNCommonEvents.wandModuleStart(data.module, player.level(), player, null)
                    if (t) player.setPerformingModule(data.module, true)
                }


            }.exceptionally {
                CypherNexus.debugNetwork(Level.WARN) { it.message.toString() }
                return@exceptionally null
            }
        }

    }
}