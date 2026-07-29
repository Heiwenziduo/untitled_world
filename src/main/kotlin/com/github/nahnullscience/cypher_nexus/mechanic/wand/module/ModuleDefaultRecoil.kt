package com.github.nahnullscience.cypher_nexus.mechanic.wand.module

import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.ItemWandInstance
import com.github.nahnullscience.cypher_nexus.utility.PosDirePair
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3

class ModuleDefaultRecoil(
    override val instance: ItemWandInstance
) : AbstractRecoilModule() {

    override fun recoil(
        invoker: Entity,
        recoil: Double,
        invokePosDire: PosDirePair
    ) {
        // since it is the client side that is Player position authoritative
        // this logic should run on both side, client for smooth movement, server for verification
        val dire = if (invokePosDire.direction != Vec3.ZERO) invokePosDire.direction
        else invoker.eyePosition.vectorTo(invokePosDire.position)
        val recoil0 = recoil / 20
        invoker.push(dire.normalize().scale(recoil0).reverse())
    }
}