package com.github.nahnullscience.cypher_nexus.content.cypher.module.modules

import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.ItemWandInstance
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.AbstractPrimaryModule
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.ModuleSharedLogic
import net.minecraft.server.level.ServerPlayer
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
        super.onHoldingStart(level, invoker, stack)
    }

    override fun onHoldingTick(level: Level, invoker: LivingEntity, stack: ItemStack?, tickCount: Int) {
        println("${level.isClientSide} side onHoldingTick: $invoker")
        ModuleSharedLogic.invoking(level, invoker, stack)
    }

    override fun onHoldingStop(level: Level, invoker: LivingEntity, stack: ItemStack?, tickCount: Int) {
        super.onHoldingStop(level, invoker, stack, tickCount)

        if (invoker is ServerPlayer) {
            instance.sendSyncStatePacket(invoker)
        }
    }
}