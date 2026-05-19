package com.github.nahnullscience.cypher_nexus.mechanic.cypher

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.content.entity.CypherProjectile
import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttributeOperation
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.BeforeDiscardHook
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.FirstTickHook
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.HitEntityHook
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.TickBehaviorHook
import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategoryRegistry
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level

object _TestModifier: ModifierCypher(
    manaDrain = 60f
), TickBehaviorHook, FirstTickHook, BeforeDiscardHook, HitEntityHook {
    override val category = CypherCategoryRegistry.OTHER
    override val resource = CypherNexus.modResource("test_modifier")
    override fun tickBehaviorBoth(level: Level, projectile: CypherProjectile, strength: Int) {
        if (projectile.tickCount % 20 != 0) return
        if (level.isClientSide) println("client tick hook")
        else println("server tick hook")
    }

    override fun firstTickBoth(
        level: Level,
        projectile: CypherProjectile,
        strength: Int
    ) {
        if (level.isClientSide) println("client firstTickBoth")
        else println("server firstTickBoth")
    }

    override fun beforeDiscardBoth(
        level: Level,
        projectile: CypherProjectile,
        strength: Int,
        reason: CypherProjectile.DiscardReason
    ) {
        if (level.isClientSide) println("client beforeExpireBoth")
        else println("server beforeExpireBoth")
    }

    override fun onHitEntityServer(
        level: Level,
        projectile: CypherProjectile,
        strength: Int,
        target: Entity
    ) {
        if (level.isClientSide) println("client onHitEntityServer")
        else println("server onHitEntityServer")
    }

    init {
//         addFlag(CypherFlags.PIERCE_BLOCK)
//         addFlag(CypherFlags.NO_DAMAGE)

        addAttribute(CypherAttributes.DAMAGE, CypherAttributeOperation.ADD, 1.0)
        addAttribute(CypherAttributes.SPEED, CypherAttributeOperation.MULTIPLY_BASE, 0.5)
        addAttribute(CypherAttributes.EXISTING, CypherAttributeOperation.ADD, 60.0)
        addAttribute(CypherAttributes.CAST_DELAY, CypherAttributeOperation.ADD, 3.0)
        addAttribute(CypherAttributes.RECHARGE_TIME, CypherAttributeOperation.ADD, 5.0)
    }
}