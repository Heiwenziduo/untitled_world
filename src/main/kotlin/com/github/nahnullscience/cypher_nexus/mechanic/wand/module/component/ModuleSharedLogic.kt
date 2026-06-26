package com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component

import com.github.nahnullscience.cypher_nexus.mechanic.wand.IWandLike
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

object ModuleSharedLogic {
    fun invoking(level: Level, invoker: LivingEntity, stack: ItemStack?) {
        val wand = stack?.item as? IWandLike ?: return
        wand.tryInvoke(level, invoker, stack)
    }

}