package com.github.nahnullscience.cypher_nexus.content.cypher.module.modules

import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.ItemWandInstance
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.IInputModule
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.InputEmpty
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.types.AbstractSecondaryInputModule
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

class SecondaryEmptyModule(
    override val instance: ItemWandInstance
) : AbstractSecondaryInputModule(), IInputModule by InputEmpty {
    override val maxHoldingTick: Int = 100

    override fun onHoldingStart(level: Level, invoker: LivingEntity, stack: ItemStack) {
        InputEmpty.onHoldingStart(level, invoker, stack)
    }

    override fun onHoldingStop(level: Level, invoker: LivingEntity, stack: ItemStack) {
        InputEmpty.onHoldingStop(level, invoker, stack)
    }

    override fun onHoldingTick(level: Level, invoker: LivingEntity, stack: ItemStack) {
        InputEmpty.onHoldingTick(level, invoker, stack)
    }
}