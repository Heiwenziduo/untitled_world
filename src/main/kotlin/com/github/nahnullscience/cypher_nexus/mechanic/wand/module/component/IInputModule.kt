package com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component

import com.github.nahnullscience.cypher_nexus.mechanic.event.LivingGatherWandsEvent
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

interface IInputModule {

    /**
     * whether the original vanilla event should be canceled, if this set to true,
     * behaviors like right-click-interaction will be prevented
     * */
    abstract val consumeVanillaInput: Boolean

    /**
     * whether this module will prevent input further forward.
     * e.g. when a living is performing some modules (per tick), the method call will forward in the wand list one by one,
     * [stopBubble] setting to true will stop the forward process.
     * for example, SecondaryEmpty is false, Primary module is generally true (for that can only be performed on the MianHand).
     * the order of wands is depending on [LivingGatherWandsEvent.Active]
     * */
    abstract val stopBubble: Boolean

    /**
     *
     * */
    abstract val isHoldingInput: Boolean


    /**
     * call both sides
     * */
    abstract fun onHoldingTick(level: Level, invoker: LivingEntity, stack: ItemStack)

    /**
     * call both sides
     * */
    abstract fun onHoldingStart(level: Level, invoker: LivingEntity, stack: ItemStack)

    /**
     * call both sides
     * */
    abstract fun onHoldingStop(level: Level, invoker: LivingEntity, stack: ItemStack)

    /**
     * in the future...
     * */
//    val isToggleInput: Boolean get() = !isHoldingInput
    /**
     * in the future...
     * */
//    open fun onToggleChange(level: Level, invoker: LivingEntity, stack: ItemStack?, turnTo: Boolean) { }
}