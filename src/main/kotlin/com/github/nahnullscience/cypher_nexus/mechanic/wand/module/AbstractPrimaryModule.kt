package com.github.nahnullscience.cypher_nexus.mechanic.wand.module

import com.github.nahnullscience.cypher_nexus.init.mod.WandModuleTypes.PRIMARY
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.IWandModule
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.InputModule
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

abstract class AbstractPrimaryModule : IWandModule, InputModule {
    final override val typeHolder = PRIMARY
    final override val consumeInput = true

}