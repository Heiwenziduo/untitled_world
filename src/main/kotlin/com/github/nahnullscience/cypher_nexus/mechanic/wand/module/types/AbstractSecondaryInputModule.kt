package com.github.nahnullscience.cypher_nexus.mechanic.wand.module.types

import com.github.nahnullscience.cypher_nexus.init.mod.WandModuleTypes.SECONDARY_MODULE
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.AbstractInputModule
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack

abstract class AbstractSecondaryInputModule() : AbstractInputModule() {
    final override val moduleType = SECONDARY_MODULE

    /**
     * called inside Item#use, on both sides, Note if [consumeVanillaInput] set to true, this method may not call.
     * @return [InteractionResult], default is [InteractionResult.PASS] which forward logic to another hand
     * */
    fun onVanillaUseStart(user: LivingEntity, stack: ItemStack, hand: InteractionHand): InteractionResult {
        if (consumeVanillaInput) return InteractionResult.CONSUME

        if (isHoldingInput) user.startUsingItem(hand)
        return if (stopBubble) InteractionResult.CONSUME else InteractionResult.PASS
    }

    /**
     * if [LivingEntity.startUsingItem] is called in [onVanillaUseStart], this method will call every tick on both sides
     * */
    fun onVanillaUseTick(user: LivingEntity, stack: ItemStack, remainTicks: Int) {}

    /**
     * when using stops, this is added by neo. called on both sides
     * */
    fun onVanillaUseStop(user: LivingEntity, stack: ItemStack, remainTicks: Int) {}
}