package com.github.nahnullscience.cypher_nexus.mechanic.wand.module

import com.github.nahnullscience.cypher_nexus.init.mod.WandModuleTypes.SECONDARY
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.IWandModule
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.InputModule
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

abstract class AbstractSecondaryModule : IWandModule, InputModule {
    final override val typeHolder = SECONDARY

    /**
     * whether the original vanilla event should be canceled, if canceled, further process from other source will not perform
     * */
    abstract override val takeoverVanillaInput: Boolean

    /**
     * called inside Item#use, on both sides, Note if [takeoverVanillaInput] set to true, this method may not call.
     * @return [InteractionResult], default is [InteractionResult.PASS] which forward logic to another hand
     * */
    open fun onVanillaUseStart(level: Level, user: Player, stack: ItemStack, hand: InteractionHand): InteractionResult {
        return if (takeoverVanillaInput) InteractionResult.PASS else {
            user.startUsingItem(hand)
            InteractionResult.SUCCESS
        }
    }

    /**
     * if [Player.startUsingItem] is called in [onVanillaUseStart], this method will call on every tick on both sides
     * */
    open fun onVanillaUseTick(level: Level, user: LivingEntity, stack: ItemStack, remainTicks: Int) { }

    /**
     * last step of vanilla item-use pipeline, called on both sides
     * */
    open fun onVanillaStopUse(stack: ItemStack, user: LivingEntity, remainTicks: Int) { }
}