package com.github.nahnullscience.cypher_nexus.mechanic.cypher

import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategoryRegistry
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3

abstract class StaticProjectileCypher(
    override val manaDrain: Float
) : AbstractProjectileCypher() {
    override val category = CypherCategoryRegistry.STATIC_PROJECTILE

    final override fun createProjectile(
        level: Level,
        helper: CypherInvokerHelper,
        invoker: LivingEntity?,
        stack: ItemStack?,
        startPos: Vec3,
        direction: Vec3?,
        invokeList: List<AbstractCypher>
    ) {
        super.createProjectile(level, helper, invoker, stack, startPos, direction, invokeList)
    }
}