package com.github.nahnullscience.cypher_nexus.mechanic.cypher

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.content.entity.CypherProjectile
import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategoryRegistry
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileStateBlock
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
        invoker: LivingEntity?,
        startPos: Vec3,
        direction: Vec3?,
        shootState: ProjectileStateBlock,
        payload: ProjectileStateBlock?
    ): CypherProjectile {
        // do nothing
        // FIXME should always skip this
        return super.createProjectile(level, invoker, startPos, direction, shootState, payload)
    }

}