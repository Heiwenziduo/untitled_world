package com.github.nahnullscience.cypher_nexus.content.cypher.module.modules

import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.ItemWandInstance
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.AbstractSecondaryModule
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.IEmptyModule
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

/**
 * clear default right click behavior (which is invoking)
 * */
class ModuleSecondaryEmpty(
    override val instance: ItemWandInstance
) : AbstractSecondaryModule(), IEmptyModule {
    override val takeoverVanillaInput = false
    override val consumeInput = false
    override val isHoldingInput = false
    override val maxHoldingTick = 0

    override fun onVanillaUseStart(
        level: Level,
        user: Player,
        stack: ItemStack,
        hand: InteractionHand
    ): InteractionResult {
        return InteractionResult.PASS
    }
}