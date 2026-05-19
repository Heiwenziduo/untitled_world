package com.github.nahnullscience.cypher_nexus.client.network

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.client.gui.CypherIndexScreen
import com.github.nahnullscience.cypher_nexus.network.CypherEntitySyncData
import com.github.nahnullscience.cypher_nexus.network.client.ClientboundOpenIndexScreen
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
@OnlyIn(Dist.CLIENT)
object ClientPayloadHandler {
    fun cypherEntitySyncData(data: CypherEntitySyncData, context: IPayloadContext) {
        // Do something with the data, on the network thread, heavy computation should be done before pass to main thread
        println("client receive package -> cypherEntitySyncData: \n$data")

        // Do something with the data, on the main thread
        context.enqueueWork {

        }.exceptionally {
            // Handle exception
            // context.disconnect(Component.translatable("my_mod.networking.failed", it.message)) // this kicks player out of the logical server
            CypherNexus.LOGGER.warn(it.message)
            return@exceptionally null
        }
    }

    fun openIndexScreen(data: ClientboundOpenIndexScreen, context: IPayloadContext) {
        println("client receive package -> openIndexScreen: \n$data")

        val map = CypherUtility.sortCyphersByCategory(data.cyphersTotal) // TODO
        context.enqueueWork {
            // val player = context.player()
            Minecraft.getInstance().setScreen(CypherIndexScreen(map))
        }.exceptionally {
            CypherNexus.LOGGER.warn(it.message)
            return@exceptionally null
        }
    }
}