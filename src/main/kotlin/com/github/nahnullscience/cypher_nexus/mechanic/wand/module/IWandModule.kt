package com.github.nahnullscience.cypher_nexus.mechanic.wand.module

import com.github.nahnullscience.cypher_nexus.mechanic.wand.IWandLike
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.ItemWandInstance
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

@FunctionalInterface
interface IWandModule {
    /**
     *
     * */
    val category: ModuleCategory

    /**
     * whether the original event should be canceled, if canceled, further process from other source will not perform
     * */
    val takeoverInput: Boolean


    fun perform(level: Level, invoker: Entity, stack: ItemStack, instance: ItemWandInstance, wand: IWandLike) // or player only?
}