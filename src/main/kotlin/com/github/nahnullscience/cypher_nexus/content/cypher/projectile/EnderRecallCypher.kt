//package com.github.nahnullscience.cypher_nexus.content.cypher.projectile
//
//import com.github.nahnullscience.cypher_nexus.CypherNexus
//import com.github.nahnullscience.cypher_nexus.init.ModEntities
//import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractCypherProjectile
//import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ProjectileCypher
//import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.BothBeforeDiscardHook
//import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.BothFirstTickHook
//import net.minecraft.world.level.Level
//
//object EnderRecallCypher : ProjectileCypher() {
//    override val resource = CypherNexus.modResource("ender_recall")
//    override val projectileType = ModEntities.CYPHER_ENDER_RECALL
//
//    // just use same attributes
//    override fun defaultAttributes() = EnderTeleportationCypher.defaultAttributes().manaDrain(25f)
//}