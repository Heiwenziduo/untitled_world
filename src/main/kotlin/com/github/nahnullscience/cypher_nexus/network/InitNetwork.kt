package com.github.nahnullscience.cypher_nexus.network

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.client.network.ClientPayloadHandler
import com.github.nahnullscience.cypher_nexus.network.client.ClientboundEditWandCyphersConfirm
import com.github.nahnullscience.cypher_nexus.network.client.ClientboundOpenIndexScreen
import com.github.nahnullscience.cypher_nexus.network.client.ClientboundSyncWandInstance
import com.github.nahnullscience.cypher_nexus.network.server.ServerboundEditWandCyphers
import com.github.nahnullscience.cypher_nexus.network.server.ServerboundWandModuleEnd
import com.github.nahnullscience.cypher_nexus.network.server.ServerboundWandModuleStart
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
import net.neoforged.neoforge.network.registration.HandlerThread
import net.neoforged.neoforge.network.registration.PayloadRegistrar


@EventBusSubscriber(modid = CypherNexus.MOD_ID)
object InitNetwork {
    @SubscribeEvent
    private fun registerPayload(event: RegisterPayloadHandlersEvent) {
        /* @doc
         * If you need to do some computation that is resource intensive,
         * then the work should be done on the network thread, instead of blocking the main thread.
         * */
        val registrar: PayloadRegistrar = event.registrar("114514")
        registrar.executesOn(HandlerThread.NETWORK)


        // remain unused for now...
//        registrar.playToClient(
//            CypherEntitySyncData.TYPE,
//            CypherEntitySyncData.STREAM,
//            ClientPayloadHandler::cypherEntitySyncData
//        )

        registrar.playToClient(
            ClientboundOpenIndexScreen.TYPE,
            ClientboundOpenIndexScreen.STREAM,
            ClientPayloadHandler::openIndexScreen
        )

        registrar.playToClient(
            ClientboundSyncWandInstance.TYPE,
            ClientboundSyncWandInstance.STREAM,
            ClientPayloadHandler::syncWandInstance
        )

        registrar.playToClient(
            ClientboundEditWandCyphersConfirm.TYPE,
            ClientboundEditWandCyphersConfirm.STREAM,
            ClientPayloadHandler::editWandCyphersConfirm
        )

        registrar.playToServer(
            ServerboundEditWandCyphers.TYPE,
            ServerboundEditWandCyphers.STREAM,
            ServerboundEditWandCyphers::handler
        )

        registrar.playToServer(
            ServerboundWandModuleStart.TYPE,
            ServerboundWandModuleStart.STREAM,
            ServerboundWandModuleStart::handler
        )

        registrar.playToServer(
            ServerboundWandModuleEnd.TYPE,
            ServerboundWandModuleEnd.STREAM,
            ServerboundWandModuleEnd::handler
        )
    }
}