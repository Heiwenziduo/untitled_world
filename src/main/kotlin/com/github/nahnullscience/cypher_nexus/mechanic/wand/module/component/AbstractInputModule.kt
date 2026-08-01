package com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

/**
 * handle user inputs
 * */
abstract class AbstractInputModule : AbstractWandModule(), ITypeUniqueModule, IInputModule {
    abstract val maxHoldingTick: Int

    val isHolding get() = holdingTicks > 0

    var holdingTicks: Int = 0
        private set

    override fun onHoldingTick(level: Level, invoker: LivingEntity, stack: ItemStack) {
        holdingTicks ++
    }

    override fun onHoldingStart(level: Level, invoker: LivingEntity, stack: ItemStack) {
    }

    override fun onHoldingStop(level: Level, invoker: LivingEntity, stack: ItemStack) {
        holdingTicks = 0
    }
}