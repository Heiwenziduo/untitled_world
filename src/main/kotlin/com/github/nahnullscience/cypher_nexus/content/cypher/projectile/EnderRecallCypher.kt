package com.github.nahnullscience.cypher_nexus.content.cypher.projectile

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.content.entity.AbstractCypherProjectile
import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.BothBeforeDiscardHook
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.BothFirstTickHook
import net.minecraft.world.level.Level

object EnderRecallCypher : ProjectileCypher(
    manaDrain = 25f
), BothBeforeDiscardHook, BothFirstTickHook {
    override val resource = CypherNexus.modResource("ender_recall")
    override fun beforeDiscardBoth(
        level: Level,
        projectile: AbstractCypherProjectile,
        strength: Int,
        reason: AbstractCypherProjectile.DiscardReason
    ) {
        EnderTeleportationCypher.beforeDiscardBoth(level, projectile, strength, reason)
    }

    override fun firstTickBoth(
        level: Level,
        projectile: AbstractCypherProjectile,
        strength: Int
    ) {
        if (!level.isClientSide) {
            val pos = projectile.position()
            val teleportation = AbstractCypherProjectile.Companion.from(level, EnderTeleportationCypher, projectile.owner, )
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