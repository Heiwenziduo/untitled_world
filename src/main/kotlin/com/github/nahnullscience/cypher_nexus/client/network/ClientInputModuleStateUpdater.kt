package com.github.nahnullscience.cypher_nexus.client.network

import com.github.nahnullscience.cypher_nexus.init.mod.WandModuleTypes.inputModules
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.LivingModuleCommon
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.WandModuleType
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.AbstractInputModule
import com.github.nahnullscience.cypher_nexus.network.server.ServerboundWandModuleEnd
import com.github.nahnullscience.cypher_nexus.network.server.ServerboundWandModuleStart
import net.minecraft.client.Minecraft
import net.neoforged.neoforge.client.network.ClientPacketDistributor
import java.util.function.Supplier

/**
 *
 * */
object ClientInputModuleStateUpdater {
    val mc get() = Minecraft.getInstance()

    fun <Module : AbstractInputModule> startModule(type: Supplier<out WandModuleType<Module>>) {
        val player = mc.player ?: return

        LivingModuleCommon.startIfNotPerformingThen(player, type.get()) {
            ClientPacketDistributor.sendToServer(ServerboundWandModuleStart(type.get()))
        }
    }

    fun <Module : AbstractInputModule> endModule(type: Supplier<out WandModuleType<Module>>) {
        val player = mc.player ?: return

        LivingModuleCommon.endIfPerformingThen(player, type.get()) {
            ClientPacketDistributor.sendToServer(ServerboundWandModuleEnd(type.get()))
        }
    }

    fun endAllInputModule() {
        val player = mc.player ?: return

        for (type in inputModules) {
            LivingModuleCommon.endIfPerformingThen(player, type) {
                ClientPacketDistributor.sendToServer(ServerboundWandModuleEnd(type))
            }
        }
    }
}