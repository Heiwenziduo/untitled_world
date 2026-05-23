package com.github.nahnullscience.cypher_nexus.mechanic.cypher

import com.github.nahnullscience.cypher_nexus.content.entity.AbstractCypherProjectile
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttributeOperation
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookContainer
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileNode
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileStateChunk
import net.minecraft.core.Holder
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import kotlin.reflect.KClass

abstract class AbstractProjectileCypher: AbstractCypher() {
    // TODO
    //open val projectile: KClass<AbstractCypherProjectile> = AbstractCypherProjectile::class
    open fun addToStateChunk(helper: InvokingHelper, chunk: ProjectileStateChunk): ProjectileStateChunk {
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
        parentHooks: HookContainer?
    ): AbstractCypherProjectile {
        val projectile = AbstractCypherProjectile(level, invoker, this, direction, shootState, node, parentHooks)
        projectile.setPos(startPos)
        return projectile
    }

    final override fun addAttribute(holder: Holder<CypherAttribute>, base: Double): AbstractCypher = super.addAttribute(holder, base)

    fun getAttrBaseOrDefault(holder: Holder<CypherAttribute>) =
        attributeMap[holder]?.get(CypherAttributeOperation.BASE)?: holder.value().defaultValue
    fun getAttrBaseOrDefault(attr: CypherAttribute) = getAttrBaseOrDefault(attr.attrRegistryHolder())

    override fun triggerCanAttach() = true
    override fun triggerCanPayload() = true

    // due to cost, should prioritise these to hook on expire
    /** called when projectile hits something
     * @param level on client side. due to cost, should prioritise these to hook-on-expire */
    open fun visualEffectOnHit(level: Level, projectile: AbstractCypherProjectile) {}
    /** called when projectile naturally expire
    * @param level on client side. due to cost, should prioritise these to hook-on-expire */
    open fun visualEffectOnExpire(level: Level, projectile: AbstractCypherProjectile) {}
}