package com.github.nahnullscience.cypher_nexus.mechanic.cypher

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.DedicatedCypherProjectile
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookContainer
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileNode
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk
import net.minecraft.core.Holder
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.phys.Vec3
import java.util.function.Supplier

abstract class AbstractProjectileCypher <CY> (
    defaultAttribute: CypherDataMap.Builder.() -> CypherDataMap.Builder = NONE
) : AbstractCypher(defaultAttribute) where CY : Entity, CY : ICypherEntity {
    abstract val projectileType: Supplier<out EntityType<out CY>>

    open fun addToStateChunk(chunk: ShotStateChunk): ShotStateChunk {
        val node = ProjectileNode(this, null)
        return chunk.addProjectile(node) // forward state
    }

    open fun createProjectile(
        level: ServerLevel,
        invoker: Entity?,
        shootState: ShotStateChunk,
        node: ProjectileNode,
        stateHooks: HookContainer?
    ): CY {
        val proj = DedicatedCypherProjectile.create(
            this,
            projectileType.get(),
            level,
            invoker,
            shootState,
            node,
        )
        return proj
    }

    fun getAttrBaseOrDefault(holder: Holder<CypherAttribute>) = getAttrBaseOrDefault(holder.value())
    fun getAttrBaseOrDefault(attr: CypherAttribute): Double = attributes().projectile[attr] ?: attr.defaultValue

    override fun triggerInterplay() = true
}