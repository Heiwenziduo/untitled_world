package com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component

import com.github.nahnullscience.cypher_nexus.mechanic.event.LivingGatherWandsEvent
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

interface InputModule {
    /**
     * whether the original vanilla event should be canceled, if canceled, further process from other source will not perform
     * */
    val takeoverVanillaInput: Boolean

    /**
     * whether this module will prevent input further forward.
     * e.g. when a living is performing some modules (per tick), the method call will forward in the wand list one by one,
     * [consumeInput] setting to true will stop the forward process.
     * for example, SecondaryEmpty is false, any Primary module is always true (for that can only be performed on the MianHand).
     * the order of wands is depending on [LivingGatherWandsEvent.Active.wands]
     * */
    val consumeInput: Boolean

    /**
     *
     * */
    val isHoldingInput: Boolean
    val maxHoldingTick: Int

    /**
     *
     * */
    val isToggleInput: Boolean
        get() = !isHoldingInput

    /**
     *
     * */
    fun onHoldingTick(level: Level, invoker: LivingEntity, stack: ItemStack?, tickCount: Int) {

    }

    /**
     *
     * */
    fun onHoldingStart(level: Level, invoker: LivingEntity, stack: ItemStack?) {

    }

    /**
     *
     * */
    fun onHoldingStop(level: Level, invoker: LivingEntity, stack: ItemStack?, tickCount: Int) {

    }

    /**
     *
     * */
    fun onToggle(turnTo: Boolean) {}
}