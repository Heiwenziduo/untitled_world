package com.github.nahnullscience.cypher_nexus.client.network

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.client.gui.CypherIndexScreen
import com.github.nahnullscience.cypher_nexus.init.ModDataAttachments.WAND_DATA_MAP
import com.github.nahnullscience.cypher_nexus.network.client.ClientboundOpenIndexScreen
import com.github.nahnullscience.cypher_nexus.network.client.ClientboundSyncWandInstance
import com.github.nahnullscience.cypher_nexus.utility.mod.CypherUtility
import net.minecraft.client.Minecraft
import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn
import net.neoforged.neoforge.network.handling.IPayloadContext

/*
 * define payload TYPE by implementing CustomPacketPayload ->
 * register payload through RegisterPayloadHandlersEvent ->
 * register payload handler on desired side (client here) ->
 * use PacketDistributor.sendToPlayer method to send package
 * */
object ClientPayloadHandler {

    fun openIndexScreen(data: ClientboundOpenIndexScreen, context: IPayloadContext) {
        // Do something with the data, on the network thread, heavy computation should be done before pass to main thread
//        println("client receive package -> openIndexScreen: \n$data")

        val map = CypherUtility.sortCyphersByCategory(data.cyphersTotal) // TODO
        context.enqueueWork {
            // Do something with the data, on the main thread

            // val player = context.player()
            Minecraft.getInstance().setScreen(CypherIndexScreen(map))
        }.exceptionally {
            // Handle exception
            // context.disconnect(Component.translatable("my_mod.networking.failed", it.message)) // this kicks player out of the logical server
            CypherNexus.LOGGER.warn(it.message)
            return@exceptionally null
        }
    }

    fun syncWandInstance(data: ClientboundSyncWandInstance, context: IPayloadContext) {
        println("client receive package -> syncWandInstance: \n$data")
        val player = context.player()
        player.getData(WAND_DATA_MAP)[data.uuid]?.syncDataClient(
            data.mana,
            data.delay,
            data.recharge,
            data.deck
        )
    }
}