package com.github.nahnullscience.cypher_nexus.content.cypher.wand_module.modules

import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.ItemWandInstance
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.IInputModule
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.InputEmptyDelegation
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.types.AbstractSecondaryInputModule
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

class SecondaryEmptyModule(
    override val instance: ItemWandInstance
) : AbstractSecondaryInputModule(), IInputModule by InputEmptyDelegation {

    override fun onHoldingStart(level: Level, invoker: LivingEntity, stack: ItemStack) {
        InputEmptyDelegation.onHoldingStart(level, invoker, stack)
    }

    override fun onHoldingStop(level: Level, invoker: LivingEntity, stack: ItemStack) {
        InputEmptyDelegation.onHoldingStop(level, invoker, stack)
    }

    override fun onHoldingTick(level: Level, invoker: LivingEntity, stack: ItemStack) {
        InputEmptyDelegation.onHoldingTick(level, invoker, stack)
    }
}