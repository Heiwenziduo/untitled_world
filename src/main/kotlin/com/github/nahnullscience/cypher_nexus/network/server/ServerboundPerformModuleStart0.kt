//package com.github.nahnullscience.cypher_nexus.network.server
//
//import com.github.nahnullscience.cypher_nexus.CypherNexus
//import com.github.nahnullscience.cypher_nexus.init.ModDataAttachments.WAND_DATA_MAP
//import com.github.nahnullscience.cypher_nexus.init.mod.WandModuleTypes.RESOURCE_KEY
//import com.github.nahnullscience.cypher_nexus.mechanic.wand.IWandLike
//import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.WandModuleType
//import net.minecraft.network.RegistryFriendlyByteBuf
//import net.minecraft.network.codec.ByteBufCodecs
//import net.minecraft.network.codec.StreamCodec
//import net.minecraft.network.protocol.common.custom.CustomPacketPayload
//import net.neoforged.neoforge.network.handling.IPayloadContext
//import org.apache.logging.log4j.Level
//
//data class ServerboundPerformModuleStart0(
//    val uuid: String,
//    val module: WandModuleType<*>,
//    val wandSlot: Int
//) : CustomPacketPayload {
//    override fun type() = TYPE
//
//    companion object {
//        val TYPE: CustomPacketPayload.Type<ServerboundPerformModuleStart0> =
//            CustomPacketPayload.Type(CypherNexus.modResource("perform_module_start"))
//
//        val STREAM: StreamCodec<RegistryFriendlyByteBuf, ServerboundPerformModuleStart0> = StreamCodec.composite(
//            ByteBufCodecs.STRING_UTF8, ServerboundPerformModuleStart0::uuid,
//            ByteBufCodecs.registry(RESOURCE_KEY), ServerboundPerformModuleStart0::module,
//            ByteBufCodecs.VAR_INT, ServerboundPerformModuleStart0::wandSlot,
//            ::ServerboundPerformModuleStart0
//        )
//
//        fun performWandModuleStart(data: ServerboundPerformModuleStart0, context: IPayloadContext) {
//            CypherNexus.debugNetwork { "server receive package -> performWandModule: \n$data" }
//
//            context.enqueueWork {
//                val player = context.player()
//                val instance = player.getData(WAND_DATA_MAP)[data.uuid]!!
//                val stack = player.inventory.getItem(data.wandSlot)
//
//                if (!stack.isEmpty && stack.item is IWandLike) {
//                    instance.handleGeneralInputModule(data.module, player.level(), player, stack)
//                } else {
//                    CypherNexus.debugNetwork(Level.ERROR) { "didn't find wand in slot ${data.wandSlot}" }
//                }
//            }.exceptionally {
//                CypherNexus.LOGGER.warn(it.message)
//                return@exceptionally null
//            }
//        }
//    }
//}