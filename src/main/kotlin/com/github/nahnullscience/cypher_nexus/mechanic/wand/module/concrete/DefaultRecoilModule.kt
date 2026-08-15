package com.github.nahnullscience.cypher_nexus.mechanic.wand.module.concrete

import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.ItemWandInstance
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.types.AbstractRecoilFunctionModule
import com.github.nahnullscience.cypher_nexus.utility.linear_space.CoordinateDefinition
import com.github.nahnullscience.cypher_nexus.utility.perspectiveCoordinate
import com.github.nahnullscience.cypher_nexus.utility.plus
import com.github.nahnullscience.cypher_nexus.utility.times
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import kotlin.math.sqrt

open class DefaultRecoilModule(
    override val instance: ItemWandInstance
) : AbstractRecoilFunctionModule() {
    companion object {
//        inline fun recoilDefault(power: Double, direction: Vec3, base: Vec3): Vec3 {
//            return base * 0.5 + direction * (sqrt(power) * 0.05)
//        }
    }
    override fun execute(
        invoker: LivingEntity,
        wand: ItemStack?,
        invokerCoordinate: CoordinateDefinition?,
        indirectTarget: Entity?,
        performingTicks: Int,
        power: Double
    ): Boolean {
        if (power.isFinite() && power > 1e-7) {
            // since it is the client side that is Player position authoritative
            // this logic should run on both side, client for smooth movement, server for verification

//            println("recoil power: $power")
            val coo = invokerCoordinate ?: invoker.perspectiveCoordinate()
            val dire = coo.front.reverse()
            val base = invoker.deltaMovement

//            val next = base * 0.5 + dire * (sqrt(power) * 0.05)

            invoker.deltaMovement = base + dire * (sqrt(power) * 0.05)
            if (invoker !is Player) { invoker.needsSync = true }
            return true
        }
        return false
    }
}