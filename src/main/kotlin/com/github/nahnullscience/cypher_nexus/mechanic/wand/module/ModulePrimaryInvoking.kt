package com.github.nahnullscience.cypher_nexus.mechanic.wand.module

import com.github.nahnullscience.cypher_nexus.init.ModDataAttachments.WAND_MODULE_STATE_TRACKER
import com.github.nahnullscience.cypher_nexus.mechanic.wand.IWandLike
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.ItemWandInstance
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

/**
 * make wand able to fire through left-click
 * */
class ModulePrimaryInvoking(
    override val instance: ItemWandInstance
) : AbstractPrimaryModule() {
    override val takeoverVanillaInput = true
    override val isHoldingInput = true
    override val maxHoldingTick = 72_000

    override fun onHoldingStart(level: Level, invoker: LivingEntity, stack: ItemStack?) {
        invoker.getData(WAND_MODULE_STATE_TRACKER).printCurrentPerforming()
    }

    override fun onHoldingTick(level: Level, invoker: LivingEntity, stack: ItemStack?, tickCount: Int) {
        println("${level.isClientSide} side onHoldingTick: $invoker")
        val wand = stack?.item as? IWandLike ?: return
        wand.tryInvoke(level, invoker, stack)
    }

    override fun onHoldingStop(level: Level, invoker: LivingEntity, stack: ItemStack?, tickCount: Int) {
        invoker.getData(WAND_MODULE_STATE_TRACKER).printCurrentPerforming()
    }
}