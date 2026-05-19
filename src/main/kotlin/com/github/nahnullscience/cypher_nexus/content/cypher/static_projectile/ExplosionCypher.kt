package com.github.nahnullscience.cypher_nexus.content.cypher.static_projectile

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.content.entity.CypherProjectile
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.BeforeDiscardHook
import net.minecraft.world.level.Explosion
import net.minecraft.world.level.Level

object ExplosionCypher : AbstractStaticSummoner(
    manaDrain = 20f
), BeforeDiscardHook {
    override val resource = CypherNexus.modResource("explosion")
    override fun beforeDiscardBoth(
        level: Level,
        projectile: CypherProjectile,
        strength: Int,
        reason: CypherProjectile.DiscardReason
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