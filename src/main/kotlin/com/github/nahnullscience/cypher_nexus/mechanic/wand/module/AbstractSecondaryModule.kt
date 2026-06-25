package com.github.nahnullscience.cypher_nexus.mechanic.wand.module

import com.github.nahnullscience.cypher_nexus.init.mod.WandModuleTypes.SECONDARY
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.ItemWandInstance
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.IWandModule
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.InputModule
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

abstract class AbstractSecondaryModule : IWandModule, InputModule {
    final override val typeHolder = SECONDARY

    /**
     * whether the original event should be canceled, if canceled, further process from other source will not perform.
     *
     * */
    abstract override val takeoverVanillaInput: Boolean

    abstract fun perform(level: Level, invoker: Entity, stack: ItemStack)

    /**
     * called inside Item#use
     * @return [InteractionResult], default is [InteractionResult.PASS] which forward logic to another hand
     * */
    abstract fun onInteract(player: Player, instance: ItemWandInstance, hand: InteractionHand): InteractionResult

//    open fun onVanillaUseTick(level: Level, user: LivingEntity, stack: ItemStack, remainTicks: Int) {}
//    open fun onVanillaStopUse(stack: ItemStack, user: LivingEntity, remainTicks: Int) {}
}