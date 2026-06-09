package com.github.nahnullscience.cypher_nexus.content.block

import com.github.nahnullscience.cypher_nexus.network.client.ClientboundOpenIndexScreen
import com.github.nahnullscience.cypher_nexus.utility.mod.CypherData
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult
import net.neoforged.neoforge.network.PacketDistributor

/**
 *
 * */
class CypherIndexBlock(property: BlockBehaviour.Properties): Block(property) {

    override fun useWithoutItem(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hitResult: BlockHitResult
    ): InteractionResult {
        // triggered on both logical sides
        // UntitledWorld.LOGGER.info("SpellIndexBlock clicking, side: ${ if(level.isClientSide) "client" else "server" }")
//        if (level.isClientSide) {
//            Minecraft.getInstance().setScreen(CypherIndexScreen(CypherData.cypherMap))
//        }
        if (!level.isClientSide) {
                PacketDistributor.sendToPlayer(player as ServerPlayer, ClientboundOpenIndexScreen(
                CypherData.cyphersUnhide, CypherData.cyphersPlayerUnlocked(player))
            )
        }
        return InteractionResult.SUCCESS
    }
}