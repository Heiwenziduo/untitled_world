//package com.github.nahnullscience.cypher_nexus.content.cypher.projectile
//
//import com.github.nahnullscience.cypher_nexus.CypherNexus
//import com.github.nahnullscience.cypher_nexus.init.ModEntities
//import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractCypherProjectile
//import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
//import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap
//import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ProjectileCypher
//import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
//import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.BothBeforeDiscardHook
//import net.minecraft.core.particles.ColorParticleOption
//import net.minecraft.core.particles.ParticleTypes
//import net.minecraft.core.particles.PowerParticleOption
//import net.minecraft.world.level.Level
//
//object EnderTeleportationCypher : ProjectileCypher() {
//    override val resource = CypherNexus.modResource("ender_teleportation")
//    override val projectileType = ModEntities.CYPHER_ENDER_TELEPORTATION
//
//
//    override fun defaultAttributes(): CypherDataMap.Builder {
//        return super.defaultAttributes()
//            .manaDrain(20f)
//            .flags(CypherFlags.NO_DAMAGE)
//            .projectileAttr(CypherAttributes.SPEED, 1.3)
//            .projectileAttr(CypherAttributes.EXISTING, 15.0)
//    }
//}