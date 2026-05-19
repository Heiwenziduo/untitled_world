package com.github.nahnullscience.cypher_nexus.mechanic.cypher

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategoryRegistry
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3

/**
 * default registered cypher, like blocks:air, any cypher missing a registry name will be replaced with this.
 * */
object EmptyCypher: AbstractProjectileCypher() {
    override val draw = 1
    override val hide = true
    override val category = CypherCategoryRegistry.OTHER
    override val resource = CypherNexus.modResource("empty_cypher")
    init {

    }

    override fun createProjectile(
        level: Level,
        helper: CypherInvokerHelper,
        invoker: LivingEntity?,
        stack: ItemStack?,
        startPos: Vec3,
        direction: Vec3?,
        invokeList: List<AbstractCypher>
    ) {
        // do nothing
    }

    override fun onInvokeServer(
        level: Level,
        caster: Entity?,
        stack: ItemStack?,
        helper: CypherInvokerHelper,
        wandLength: Float
    ) {
        // do nothing
    }
}