package com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component

import com.github.nahnullscience.cypher_nexus.utility.perspectiveCoordinate
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

/**
 * handle user inputs.
 *
 * as convention, `InputModule` should be the sole `Coordinate` provider along the invoking process.
 *
 * input-modules -> call invoke-module -> item-wands #tryInvoke -> get adjusted by wand ->
 * get adjusted by hooks #redirectInvoking -> apply pattern layout -> cypher-entity #initDirection
 * */
abstract class AbstractInputModule : AbstractWandModule(), ITypeUniqueModule, IInputModule {

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

    fun perspectiveCoordinate(invoker: LivingEntity) = invoker.perspectiveCoordinate()
}