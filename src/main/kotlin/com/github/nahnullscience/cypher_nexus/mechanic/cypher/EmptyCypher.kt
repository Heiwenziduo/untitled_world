package com.github.nahnullscience.cypher_nexus.mechanic.cypher

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.content.entity.AbstractCypherProjectile
import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategories
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookContainer
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileNode
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileStateChunk
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3

/**
 * default registered cypher, like blocks:air, any cypher missing a registry name will be replaced with this.
 * */
object EmptyCypher: AbstractProjectileCypher() {
    override val draw = 0
    override val hide = true
    override val category = CypherCategories.OTHER
    override val resource = CypherNexus.modResource("empty_cypher")
    override fun isInvokable() = false
    init {

    }

    override fun createProjectile(
        level: Level,
        invoker: Entity?,
        startPos: Vec3,
        direction: Vec3?,
        shootState: ProjectileStateChunk,
        node: ProjectileNode,
        parentHooks: HookContainer?
    ): AbstractCypherProjectile {
        // do nothing
        // FIXME should always skip this
        CypherNexus.LOGGER.debug("Empty#createProjectile is called.")
        return super.createProjectile(level, invoker, startPos, direction, shootState, node, null)
    }

}