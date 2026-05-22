package com.github.nahnullscience.cypher_nexus.mechanic.cypher

import com.github.nahnullscience.cypher_nexus.content.entity.AbstractCypherProjectile
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttributeOperation
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookContainer
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileNode
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileStateBlock
import net.minecraft.core.Holder
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3

abstract class AbstractProjectileCypher: AbstractCypher() {
//    abstract val projectile: KClass<CypherProjectile>
    open fun addToState(helper: InvokingHelper, state: ProjectileStateBlock): ProjectileStateBlock {
        val node = ProjectileNode(this, null)
        return state.addProjectile(node) // forward state
    }

    open fun createProjectile(
        level: Level,
        invoker: Entity?,
        startPos: Vec3,
        direction: Vec3?,
        shootState: ProjectileStateBlock,
        payload: ProjectileStateBlock?,
        parentHooks: HookContainer?
    ): AbstractCypherProjectile {
        val projectile = AbstractCypherProjectile(level, invoker, this, direction, shootState, payload, parentHooks)
        projectile.setPos(startPos)
        return projectile
    }

    final override fun addAttribute(holder: Holder<CypherAttribute>, base: Double): AbstractCypher = super.addAttribute(holder, base)

    fun getAttrBaseOrDefault(holder: Holder<CypherAttribute>) =
        attributeMap[holder]?.get(CypherAttributeOperation.BASE)?: holder.value().defaultValue
    fun getAttrBaseOrDefault(attr: CypherAttribute) = getAttrBaseOrDefault(attr.attrRegistryHolder())


    // due to cost, should prioritise these to hook on expire
    /** called when projectile hits something
     * @param level on client side. due to cost, should prioritise these to hook-on-expire */
    open fun visualEffectOnHit(level: Level, projectile: AbstractCypherProjectile) {}
    /** called when projectile naturally expire
    * @param level on client side. due to cost, should prioritise these to hook-on-expire */
    open fun visualEffectOnExpire(level: Level, projectile: AbstractCypherProjectile) {}
}