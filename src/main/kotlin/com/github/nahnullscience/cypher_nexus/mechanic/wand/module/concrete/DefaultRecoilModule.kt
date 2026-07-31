package com.github.nahnullscience.cypher_nexus.mechanic.wand.module.concrete

import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.ItemWandInstance
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.types.AbstractRecoilFunctionModule
import com.github.nahnullscience.cypher_nexus.utility.CoordinateDefinition
import com.github.nahnullscience.cypher_nexus.utility.perspectiveCoordinate
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack

class DefaultRecoilModule(
    override val instance: ItemWandInstance
) : AbstractRecoilFunctionModule() {
    override fun execute(
        invoker: LivingEntity,
        invokerCoordinate: CoordinateDefinition?,
        indirectTarget: Entity?,
        wand: ItemStack?,
        performingTicks: Int?,
        power: Double?
    ): Boolean {
        power ?: return false
        // since it is the client side that is Player position authoritative
        // this logic should run on both side, client for smooth movement, server for verification
        val coo = invokerCoordinate ?: invoker.perspectiveCoordinate()
        val dire = coo.front.reverse()
        val recoil = power / 20
        invoker.push(dire.scale(recoil))
        return true
    }
}