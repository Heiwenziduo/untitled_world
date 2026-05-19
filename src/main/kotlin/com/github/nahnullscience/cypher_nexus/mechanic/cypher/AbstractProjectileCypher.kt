package com.github.nahnullscience.cypher_nexus.mechanic.cypher

import com.github.nahnullscience.cypher_nexus.content.entity.CypherProjectile
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttributeOperation
import com.github.nahnullscience.cypher_nexus.utility.mod.PosDirePair
import net.minecraft.core.Holder
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3

abstract class AbstractProjectileCypher: AbstractCypher() {
    fun createProjectile(
        level: Level, helper: CypherInvokerHelper, invoker: LivingEntity?,
        stack: ItemStack?, posDirePair: PosDirePair, invokeList: List<AbstractCypher> = listOf()
    ) = createProjectile(level, helper, invoker, stack, posDirePair.position, posDirePair.direction, invokeList)
     open fun createProjectile(
         level: Level, helper: CypherInvokerHelper, invoker: LivingEntity?,
         stack: ItemStack?, startPos: Vec3, direction: Vec3?, invokeList: List<AbstractCypher>
     ) {
        val projectile = CypherProjectile(level, invoker, this, helper, direction?.normalize(), invokeList)
        projectile.setPos(startPos)
        level.addFreshEntity(projectile)
    }
    final override fun addAttribute(holder: Holder<CypherAttribute>, base: Double): AbstractCypher = super.addAttribute(holder, base)

    fun getAttrBaseOrDefault(holder: Holder<CypherAttribute>) =
        attributeMap[holder]?.get(CypherAttributeOperation.BASE)?: holder.value().defaultValue
    fun getAttrBaseOrDefault(attr: CypherAttribute) = getAttrBaseOrDefault(attr.attrRegistryHolder())


    // due to cost, should prioritise these to hook on expire
    /** called when projectile hits something
     * @param level on client side. due to cost, should prioritise these to hook-on-expire */
    open fun visualEffectOnHit(level: Level, projectile: CypherProjectile) {}
    /** called when projectile naturally expire
    * @param level on client side. due to cost, should prioritise these to hook-on-expire */
    open fun visualEffectOnExpire(level: Level, projectile: CypherProjectile) {}
}