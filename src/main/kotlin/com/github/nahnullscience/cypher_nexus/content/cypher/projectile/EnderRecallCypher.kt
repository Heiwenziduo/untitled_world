package com.github.nahnullscience.cypher_nexus.content.cypher.projectile

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.content.entity.AbstractCypherProjectile
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.BothBeforeDiscardHook
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.BothFirstTickHook
import net.minecraft.world.level.Level

object EnderRecallCypher : ProjectileCypher(), BothBeforeDiscardHook, BothFirstTickHook {
    override val resource = CypherNexus.modResource("ender_recall")
    override fun beforeDiscardBoth(
        level: Level,
        projectile: AbstractCypherProjectile,
        strength: Int,
        reason: AbstractCypherProjectile.DiscardReason
    ) = EnderTeleportationCypher.beforeDiscardBoth(level, projectile, strength, reason)

    override fun firstTickBoth(
        level: Level,
        projectile: AbstractCypherProjectile,
        strength: Int
    ) {
        if (!level.isClientSide) {
            val pos = projectile.position()
            val teleportation = AbstractCypherProjectile.from(level, EnderTeleportationCypher, projectile.owner, )
            teleportation.setPos(pos)
            teleportation.existing = 100 // recall after 5seconds, at most
            level.addFreshEntity(teleportation)
        }
    }

    // just use same attributes
    override fun defaultAttributes() = EnderTeleportationCypher.defaultAttributes().manaDrain(25f)
}