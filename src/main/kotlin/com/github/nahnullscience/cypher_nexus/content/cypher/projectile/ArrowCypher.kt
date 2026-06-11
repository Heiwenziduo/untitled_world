//package com.github.nahnullscience.cypher_nexus.content.cypher.projectile
//
//import com.github.nahnullscience.cypher_nexus.CypherNexus
//import com.github.nahnullscience.cypher_nexus.init.ModEntities
//import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractCypherProjectile
//import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
//import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap
//import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ProjectileCypher
//import net.minecraft.core.particles.ItemParticleOption
//import net.minecraft.core.particles.ParticleTypes
//import net.minecraft.world.item.ItemStack
//import net.minecraft.world.item.Items
//import net.minecraft.world.level.Level
//
//object ArrowCypher : ProjectileCypher() {
//    override val resource = CypherNexus.modResource("arrow")
//    override val projectileType = ModEntities.CYPHER_ARROW
//
//    override fun defaultAttributes(): CypherDataMap.Builder {
//        return super.defaultAttributes()
//            .manaDrain(10f)
//            .delay(2)
//            .projectileAttr(CypherAttributes.DAMAGE, 3.0)
//            .projectileAttr(CypherAttributes.SPEED, 1.1)
//            .projectileAttr(CypherAttributes.EXISTING, 300.0)
//            .projectileAttr(CypherAttributes.GRAVITY_FACTOR, 0.01)
//    }
//
//}