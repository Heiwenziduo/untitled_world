package com.github.nahnullscience.cypher_nexus.content.cypher.modifier

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap.Builder
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ModifierCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.TickBehaviorHook
import com.github.nahnullscience.cypher_nexus.utility.randomInCone
import com.github.nahnullscience.cypher_nexus.utility.times
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level

class ChaoticPathCypher(
    defaultAttribute: Builder.() -> Builder
) : ModifierCypher(defaultAttribute), TickBehaviorHook {
    override val resource = CypherNexus.modResource("chaotic_path")
    override fun <CE> onTick(
        index: Int,
        count: Int,
        level: Level,
        cyEntity: CE
    ) where CE : Entity, CE : ICypherEntity {
        if (!level.isClientSide && cyEntity.tickCount >= cyEntity.hooksSharedData.chaoticPathTick) {
            /*
             * [Entity.tickCount] is asymmetric on both sides, this is because when server spawns an entity it must ship
             * the spawn packet through network, during which time the server may have ticked the entity a few times.
             *
             * Plus, any side specific retrace of [random] `next` will cause the entire sequence shifting.
             * Even if the seeds started identical, the client and server will pull completely different values
             *
             * so the solution here is do sync immediately
             * */
            val random = cyEntity.random
            cyEntity.hooksSharedData.chaoticPathTick = random.nextInt(27) + 7 + cyEntity.tickCount
            val ang = random.nextDouble() * 135.0
            val spe = random.nextDouble() * 0.8 + 0.3
            cyEntity.deltaMovement = cyEntity.deltaMovement.randomInCone(ang, random) * spe
            cyEntity.needsSync = true
        }
    }
}