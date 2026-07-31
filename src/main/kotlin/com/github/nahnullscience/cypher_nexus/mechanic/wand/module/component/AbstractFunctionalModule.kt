package com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component

import com.github.nahnullscience.cypher_nexus.utility.CoordinateDefinition
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack

/**
 * a function module represents an invokable feature of wands, which can be invoked through input.
 * e.g. invoke, recoil.
 * a function module won't do anything till some [AbstractInputModule] try to invoke it.
 * */
abstract class AbstractFunctionalModule : AbstractWandModule(), ITypeUniqueModule {

    /**
     *
     * */
    abstract fun execute(
        invoker: LivingEntity,
        invokerCoordinate: CoordinateDefinition? = null,
        indirectTarget: Entity? = null,
        wand: ItemStack? = null,
        performingTicks: Int? = null,
        power: Double? = null,
    ): Boolean
}
