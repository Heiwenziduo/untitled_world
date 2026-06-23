package com.github.nahnullscience.cypher_nexus.mechanic.wand.module

import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.IWandModule
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.InputModule
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.ModuleCategory
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

abstract class AbstractPrimaryModule : IWandModule, InputModule {
    final override val category = ModuleCategory.PRIMARY

    /**
     * main function to perform the module, called on both sides
     * */
    abstract fun perform(level: Level, invoker: Entity, stack: ItemStack)

}