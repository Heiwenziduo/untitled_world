package com.github.nahnullscience.cypher_nexus.content.cypher.modifier

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap.Builder
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ModifierCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.HitEntityHook
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.HitResult.Type
import kotlin.math.max

class FieryCypher(
    defaultAttribute: Builder.() -> Builder
) : ModifierCypher(defaultAttribute), HitEntityHook {
    override val resource = CypherNexus.modResource("fiery")
    override fun <CE> onHit(
        index: Int,
        count: Int,
        level: Level,
        cyEntity: CE,
        result: HitResult
    ) where CE : Entity, CE : ICypherEntity {
        if (result is EntityHitResult && result.type != Type.MISS) {
            val target = result.entity
            target.remainingFireTicks = max(target.remainingFireTicks, 200)
        }
    }
}