package com.github.nahnullscience.cypher_nexus.mechanic.wand.module

import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.ItemWandInstance
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.ModuleSharedLogic
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

class ModuleDefaultSecondary(
    override val instance: ItemWandInstance,
) : AbstractSecondaryModule() {
    override val takeoverVanillaInput: Boolean = false
    override val consumeInput: Boolean = true
    override val isHoldingInput: Boolean = true
    override val maxHoldingTick: Int = 72_000


    override fun onHoldingTick(level: Level, invoker: LivingEntity, stack: ItemStack?, tickCount: Int) {
        // ModuleSharedLogic.invoking(level, invoker, stack)
    }

    override fun onVanillaUseTick(level: Level, user: LivingEntity, stack: ItemStack, remainTicks: Int) {
        ModuleSharedLogic.invoking(level, user, stack)
    }

    override fun onVanillaStopUse(stack: ItemStack, user: LivingEntity, remainTicks: Int) {
        if (user is ServerPlayer) {
            val useTime = stack.item.getUseDuration(stack, user) - remainTicks
            if (user.level().gameTime - useTime >= instance.lastInvokeTime) return  // stop sync if no conduction performed
            instance.sendSyncStatePacket(user)
        }
    }
}