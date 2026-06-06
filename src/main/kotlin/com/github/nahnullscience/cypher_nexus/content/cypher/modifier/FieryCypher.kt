package com.github.nahnullscience.cypher_nexus.content.cypher.modifier

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractCypherProjectile
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ModifierCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.ServerHitEntityHook
import net.minecraft.world.level.Level
import net.minecraft.world.phys.HitResult

object FieryCypher : ModifierCypher(), ServerHitEntityHook {
    override val resource = CypherNexus.modResource("fiery")

    override fun defaultAttributes(): CypherDataMap.Builder {
        return super.defaultAttributes()
            .manaDrain(5f)
            .flags(CypherFlags.WITH_FIRE)
    }

    override fun onHitServer(level: Level, projectile: AbstractCypherProjectile, strength: Int, result: HitResult) {
//        if (target is LivingEntity) {
//            target.hurt()
//        }
        // TODO
        // target.remainingFireTicks = min(target.remainingFireTicks + 100, 300)
    }
}