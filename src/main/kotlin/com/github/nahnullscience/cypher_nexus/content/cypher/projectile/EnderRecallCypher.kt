package com.github.nahnullscience.cypher_nexus.content.cypher.projectile

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.content.entity.CypherProjectile
import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.BeforeDiscardHook
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.FirstTickHook
import net.minecraft.world.level.Level

object EnderRecallCypher : ProjectileCypher(
    manaDrain = 25f
), BeforeDiscardHook, FirstTickHook {
    override val resource = CypherNexus.modResource("ender_recall")
    override fun beforeDiscardBoth(
        level: Level,
        projectile: CypherProjectile,
        strength: Int,
        reason: CypherProjectile.DiscardReason
    ) {
        EnderTeleportationCypher.beforeDiscardBoth(level, projectile, strength, reason)
    }

    override fun firstTickBoth(
        level: Level,
        projectile: CypherProjectile,
        strength: Int
    ) {
        if (!level.isClientSide) {
            val pos = projectile.position()
            val teleportation = CypherProjectile.Companion.from(level, EnderTeleportationCypher, projectile.owner, )
            teleportation.setPos(pos)
            teleportation.existing = 100 // recall after 5seconds, at most
            teleportation.gravity = 0.01f
            level.addFreshEntity(teleportation)
        }
    }

    init {
        // Q: If I want to keep two cypher's attributes always the same?
        addFlag(CypherFlags.NO_DAMAGE)
        addAttribute(CypherAttributes.SPEED, 1.3)
        addAttribute(CypherAttributes.EXISTING, 15.0)
    }
}