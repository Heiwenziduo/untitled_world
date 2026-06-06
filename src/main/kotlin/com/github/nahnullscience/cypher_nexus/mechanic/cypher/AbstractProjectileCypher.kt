package com.github.nahnullscience.cypher_nexus.mechanic.cypher

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractCypherProjectile
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttributeOperation
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookContainer
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileNode
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileStateChunk
import net.minecraft.core.Holder
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import java.util.function.Supplier

abstract class AbstractProjectileCypher : AbstractCypher() {
    abstract val projectileType: Supplier<out EntityType<out AbstractCypherProjectile>>

    open fun addToStateChunk(chunk: ProjectileStateChunk): ProjectileStateChunk {
        val node = ProjectileNode(this, null)
        return chunk.addProjectile(node) // forward state
    }

    open fun createProjectile(
        level: Level,
        invoker: Entity?,
        startPos: Vec3,
        direction: Vec3?,
        shootState: ProjectileStateChunk,
        node: ProjectileNode,
        stateHooks: HookContainer?
    ): AbstractCypherProjectile {
        val proj = AbstractCypherProjectile.create(
            projectileType.get(),
            level,
            invoker,
            direction,
            shootState,
            node,
            stateHooks
        )
        proj.setPos(startPos)
        return proj
    }

    fun getAttrBaseOrDefault(holder: Holder<CypherAttribute>) = getAttrBaseOrDefault(holder.value())
    fun getAttrBaseOrDefault(attr: CypherAttribute): Double {
        val opMap = attributes().projectile[attr]
        return opMap?.get(CypherAttributeOperation.BASE) ?: attr.defaultValue
    }

    override fun triggerInterplay() = true

    // due to cost, should prioritise these to hook on expire
//    /** called when projectile hits something
//     * @param level on client side. due to cost, should prioritise these to hook-on-expire */
//    open fun visualEffectOnHit(level: Level, projectile: AbstractCypherProjectile) {}
//    /** called when projectile naturally expire
//    * @param level on client side. due to cost, should prioritise these to hook-on-expire */
//    open fun visualEffectOnExpire(level: Level, projectile: AbstractCypherProjectile) {}
}