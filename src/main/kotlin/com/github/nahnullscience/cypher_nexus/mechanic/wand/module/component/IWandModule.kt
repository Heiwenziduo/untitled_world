package com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component

import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.ItemWandInstance
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import java.util.function.Supplier

interface IWandModule {
    /**
     * registered wand module type
     * */
    val typeHolder: Supplier<out WandModuleType<*>>

    /**
     * instance reference
     * */
    val instance: ItemWandInstance


    fun isTypeOf(type: Supplier<out WandModuleType<*>>) = type == typeHolder
}