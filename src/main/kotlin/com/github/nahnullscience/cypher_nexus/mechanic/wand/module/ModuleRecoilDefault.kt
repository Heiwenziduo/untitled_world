package com.github.nahnullscience.cypher_nexus.mechanic.wand.module

import com.github.nahnullscience.cypher_nexus.mechanic.wand.IWandLike
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.ItemWandInstance
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

object ModuleRecoilDefault : IWandModule {
    override val category = ModuleCategory.RECOIL
    override val takeoverInput = false

    override fun perform(level: Level, invoker: Entity, stack: ItemStack, instance: ItemWandInstance, wand: IWandLike) {

    }
}