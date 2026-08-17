package com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

/**
 * do nothing on itself and forward input instruction
 * */
object InputEmptyDelegation : IInputModule {

    override val consumeVanillaInput: Boolean = false
    override val stopBubble: Boolean = false
    override val isHoldingInput: Boolean = false
    override val maxHoldingTick: Int = 100

    override fun onHoldingTick(level: Level, invoker: LivingEntity, stack: ItemStack) = Unit
    override fun onHoldingStart(level: Level, invoker: LivingEntity, stack: ItemStack) = Unit
    override fun onHoldingStop(level: Level, invoker: LivingEntity, stack: ItemStack) = Unit
}