package com.github.nahnullscience.cypher_nexus.content.cypher.projectile

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.content.entity.AbstractCypherProjectile
import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.HookBeforeDiscardBoth
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.level.Level

object EnderTeleportationCypher : ProjectileCypher(
    manaDrain = 20f
), HookBeforeDiscardBoth {
    override val resource = CypherNexus.modResource("ender_teleportation")
    override fun beforeDiscardBoth(
        level: Level,
        projectile: AbstractCypherProjectile,
        strength: Int,
        reason: AbstractCypherProjectile.DiscardReason
    ) {
        val pos = projectile.position()
        if (!level.isClientSide) {
            projectile.owner?.teleportTo(pos.x, pos.y, pos.z)
        }
        for (i in 0..7) {
            level.addParticle(
                ParticleTypes.DRAGON_BREATH,
                pos.x, pos.y, pos.z,
                0.0, -0.1, 0.0)
        }
    }

    init {
        addFlag(CypherFlags.NO_DAMAGE)
        addAttribute(CypherAttributes.SPEED, 1.3)
        addAttribute(CypherAttributes.EXISTING, 15.0)
    }
}