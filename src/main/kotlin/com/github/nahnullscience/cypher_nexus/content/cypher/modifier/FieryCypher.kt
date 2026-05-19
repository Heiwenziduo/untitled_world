package com.github.nahnullscience.cypher_nexus.content.cypher.modifier

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.content.entity.CypherProjectile
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ModifierCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.HitEntityHook
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import kotlin.math.min

object FieryCypher : ModifierCypher(
    manaDrain = 5f
), HitEntityHook {
    override val resource = CypherNexus.modResource("fiery")
    init {
        addFlag(CypherFlags.WITH_FIRE)
    }
    override fun onHitEntityServer(
        level: Level,
        projectile: CypherProjectile,
        strength: Int,
        target: Entity
    ) {
//        if (target is LivingEntity) {
//            target.hurt()
//        }
        target.remainingFireTicks = min(target.remainingFireTicks + 100, 300)
    }
}