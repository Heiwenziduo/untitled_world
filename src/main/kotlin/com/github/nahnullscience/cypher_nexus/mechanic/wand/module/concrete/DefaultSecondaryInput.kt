package com.github.nahnullscience.cypher_nexus.mechanic.wand.module.concrete

import com.github.nahnullscience.cypher_nexus.init.mod.WandModuleTypes.INVOKE_MODULE
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.ItemWandInstance
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.types.AbstractSecondaryInputModule
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

class DefaultSecondaryInput(
    override val instance: ItemWandInstance
) : AbstractSecondaryInputModule() {
    override val consumeVanillaInput: Boolean = false
    override val stopBubble: Boolean = true
    override val isHoldingInput: Boolean = true
    override val maxHoldingTick: Int = 72_000

    override fun onHoldingTick(level: Level, invoker: LivingEntity, stack: ItemStack?) {
        super.onHoldingTick(level, invoker, stack)
        instance.functionModule(INVOKE_MODULE.get(), invoker)
    }

    override fun onHoldingStop(level: Level, invoker: LivingEntity, stack: ItemStack?) {
        super.onHoldingStop(level, invoker, stack)

        if (invoker is ServerPlayer) {
            if (invoker.level().gameTime - holdingTicks >= instance.lastInvokeTime) return  // don't sync if no conduction performed
            instance.sendSyncStatePacket(invoker)
        }
    }
}