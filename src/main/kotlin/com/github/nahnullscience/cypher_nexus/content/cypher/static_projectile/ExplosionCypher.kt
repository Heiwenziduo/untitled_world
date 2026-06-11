package com.github.nahnullscience.cypher_nexus.content.cypher.static_projectile

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.ModEntities
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractCypherProjectile
import net.minecraft.world.level.Explosion
import net.minecraft.world.level.Level

object ExplosionCypher : AbstractStaticSummonerCypher() {
    override val resource = CypherNexus.modResource("explosion")
    override val projectileType = ModEntities.CYPHER_EXPLOSION

    override fun defaultAttributes() = super.defaultAttributes().manaDrain(80f).delay(13).recharge(8)
}