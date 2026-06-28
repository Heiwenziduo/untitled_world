package com.github.nahnullscience.cypher_nexus.client.network

import com.github.nahnullscience.cypher_nexus.mechanic.entity.WandModuleStateTracker.Companion.isPerformingModule
import com.github.nahnullscience.cypher_nexus.mechanic.entity.WandModuleStateTracker.Companion.setPerformingModule
import com.github.nahnullscience.cypher_nexus.mechanic.event.CNCommonEvents
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.InputModule
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.WandModuleType
import com.github.nahnullscience.cypher_nexus.network.server.ServerboundPerformModuleEnd
import com.github.nahnullscience.cypher_nexus.network.server.ServerboundPerformModuleStart
import net.minecraft.client.Minecraft
import net.neoforged.neoforge.client.network.ClientPacketDistributor
import java.util.function.Supplier

/**
 *
 * */
object ClientWandModuleStateManager {
    val mc = Minecraft.getInstance()

    fun startModule(type: Supplier<out WandModuleType<*>>, module: InputModule) {
        val player = mc.player ?: return

        if (player.isPerformingModule(type)) {
            // continue
        } else {
            ClientPacketDistributor.sendToServer(ServerboundPerformModuleStart(type.get()))
            val t = CNCommonEvents.wandModuleStart(type.get(), player.level(), player, null)
            if (t) player.setPerformingModule(type, true)
        }
    }

    fun endModule(type: Supplier<out WandModuleType<*>>, module: InputModule?) {
        val player = mc.player ?: return

        if (player.isPerformingModule(type)) {
            ClientPacketDistributor.sendToServer(ServerboundPerformModuleEnd(type.get()))
            val f = CNCommonEvents.wandModuleEnd(type.get(), player.level(), player, null)
            if (f) player.setPerformingModule(type, false)
        } else {
            // continue
        }
    }

    fun endAllInputModule() {
        val player = mc.player ?: return

    }
}