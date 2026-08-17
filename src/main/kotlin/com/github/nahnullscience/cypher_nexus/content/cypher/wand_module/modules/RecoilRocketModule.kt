package com.github.nahnullscience.cypher_nexus.content.cypher.wand_module.modules

import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.ItemWandInstance
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.types.AbstractRecoilFunctionModule
import com.github.nahnullscience.cypher_nexus.utility.linear_space.AnchoredCoordinate
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack

class RecoilRocketModule(
    override val instance: ItemWandInstance
) : AbstractRecoilFunctionModule() {
    override fun execute(
        invoker: LivingEntity,
        wand: ItemStack?,
        coordinate: AnchoredCoordinate?,
        indirectTarget: Entity?,
        performingTicks: Int,
        power: Double
    ): Boolean {
        if (power.isFinite() && power > 1e-7) {
            // TODO refactor required
//            val coo = invoker.perspectiveCoordinate() // always push invoker forward
//            val dire = coo.front
//            val base = invoker.deltaMovement
//
////            val next = base * 0.5 + dire * (sqrt(power) * 0.05)
//            val next = base + dire * (sqrt(power) * 0.05)
//
//            invoker.deltaMovement = next
//            invoker.level().takeIf { it.isClientSide }?.let { level ->
//                val sqr = next.lengthSqr()
//                if (sqr < 0.125) return@let
//
//                // TODO emit & subscribe an event, so add trail on client side
//                val pos0 = invoker.oldPosition()
//                val pos = invoker.position()
//                val reverse = next.reverse()
//                val random = invoker.random
//                linearInterpolateGaps(pos0.x, pos0.y, pos0.z, pos.x, pos.y, pos.z, 0.25) { step, x, y, z ->
////                    val flash = DyeColor.byId(random.nextInt(15)).fireworkColor.let {
////                        ColorParticleOption.create(ParticleTypes.FLASH, it)
////                    }
////                    level.addParticle(flash, x, y, z, 0.0, 0.0, 0.0)
//                    level.addParticle(
//                        ParticleTypes.FIREWORK,
//                        x, y, z,
//                        reverse.x * random.nextDouble() * 0.33,
//                        reverse.y * random.nextDouble() * 0.33,
//                        reverse.z * random.nextDouble() * 0.33,
//                    )
//                }
//            }
//
//            if (invoker !is Player) { invoker.needsSync = true }
            return true
        }
        return false
    }
}