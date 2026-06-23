package com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component

import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.ItemWandInstance
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

interface IWandModule {
    /**
     *
     * */
    val category: ModuleCategory

    /**
     *
     * */
    val instance: ItemWandInstance
}