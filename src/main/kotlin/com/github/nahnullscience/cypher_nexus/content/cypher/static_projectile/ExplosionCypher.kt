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
    override fun beforeDiscardBoth(
        level: Level,
        projectile: AbstractCypherProjectile,
        strength: Int,
        reason: AbstractCypherProjectile.DiscardReason
    ) {
        if (!level.isClientSide) {
            val pos = projectile.position()
            // check  net.minecraft.world.level.ExplosionDamageCalculator  &&  Explosion.BlockInteraction
            level.explode(
                projectile,
                Explosion.getDefaultDamageSource(level, projectile),
                null,
                pos.x,
                pos.y,
                pos.z,
                4.0f,
                false,
                Level.ExplosionInteraction.TNT
            )
        }
    }

}