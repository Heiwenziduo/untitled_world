package com.github.nahnullscience.cypher_nexus.mechanic.wand.module

import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.ItemWandInstance
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.IWandModule
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.InputModule
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.ModuleCategory
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

abstract class AbstractSecondaryModule : IWandModule, InputModule {
    final override val category = ModuleCategory.SECONDARY

    abstract fun perform(level: Level, invoker: Entity, stack: ItemStack)

    /**
     * called inside Item#use, [Player.startUsingItem] should be handled here
     * @return [InteractionResult], default is [InteractionResult.PASS] which forward logic to another hand
     * */
    abstract fun onInteract(player: Player, instance: ItemWandInstance, hand: InteractionHand): InteractionResult
}