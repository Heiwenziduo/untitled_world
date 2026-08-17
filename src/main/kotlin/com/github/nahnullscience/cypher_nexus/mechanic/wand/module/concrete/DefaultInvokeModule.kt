package com.github.nahnullscience.cypher_nexus.mechanic.wand.module.concrete

import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.ItemWandInstance
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.types.AbstractInvokeFunctionModule
import com.github.nahnullscience.cypher_nexus.utility.linear_space.AnchoredCoordinate
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack

open class DefaultInvokeModule(
    override val instance: ItemWandInstance
) : AbstractInvokeFunctionModule() {

    /**
     *
     * */
    override fun execute(
        invoker: LivingEntity,
        wand: ItemStack?,
        coordinate: AnchoredCoordinate?,
        indirectTarget: Entity?,
        performingTicks: Int,
        power: Double
    ): Boolean {
        wand ?: return false
        val coo = coordinate ?: fallbackCoordinate(invoker)
        return instance.wand.tryInvoke(invoker.level(), invoker, coo, wand).success
    }
}