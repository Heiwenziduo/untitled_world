package com.github.nahnullscience.cypher_nexus.mechanic.wand.module

import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.ItemWandInstance
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

/**
 * make wand able to fire through left-click
 * */
class ModulePrimaryInvoking(
    override val instance: ItemWandInstance
) : AbstractPrimaryModule() {
    override val takeoverInput = true

    override fun perform(level: Level, invoker: Entity, stack: ItemStack) {
        instance.wand.tryInvoke(level, invoker, stack)
    }
}