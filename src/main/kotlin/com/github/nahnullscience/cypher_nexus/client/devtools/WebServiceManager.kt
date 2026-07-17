package com.github.nahnullscience.cypher_nexus.client.devtools

import com.github.nahnullscience.cypher_nexus.client.devtools.web.DevToolsServer
import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import net.neoforged.neoforge.client.ClientCommandSourceStack

/**
 * a wrapper of [DevToolsServer] bridges mc command line and http services
 * */
object WebServiceManager {
    val command: LiteralArgumentBuilder<CommandSourceStack>
        get() = Commands.literal("test_server")
        .requires(Commands.hasPermission(Commands.LEVEL_ALL))
        .then(
            Commands.literal("start")
                .executes(WebServiceManager::startServer)
        )
        .then(
            Commands.literal("stop")
                .executes(WebServiceManager::stopServer)
        )

    fun startServer(context: CommandContext<CommandSourceStack>): Int {
        val source = context.source
        DevToolsServer.start()
        source.sendSuccess(
            {
                Component.literal("§a[CypherNexus] server started. ${DevToolsServer.ip}")
            }, false)

        return Command.SINGLE_SUCCESS
    }

    fun stopServer(context: CommandContext<CommandSourceStack>): Int {
        val source = context.source
        DevToolsServer.stop()
        source.sendSuccess(
            {
                Component.literal("§a[CypherNexus] server stopped.")
            }, false)

        return Command.SINGLE_SUCCESS
    }
}