package com.github.nahnullscience.cypher_nexus.content.cypher.modifier

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.DedicatedCypherProjectile
import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ModifierCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.AttributeOperator
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation.ICypherBeforeInit
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.invoking.ServerInvokeRedirectPosHook
import com.github.nahnullscience.cypher_nexus.utility.RayCastUtility
import com.github.nahnullscience.cypher_nexus.utility.mod.PosDirePair
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3

object DaedalusCypher : ModifierCypher(), ServerInvokeRedirectPosHook {
    const val MARGIN = 0.3f

    override val resource = CypherNexus.modResource("daedalus")

    override fun defaultAttributes(): CypherDataMap.Builder {
        return super.defaultAttributes()
            .manaDrain(50f)
            .stateChunkAttr(CypherAttributes.SPEED, AttributeOperator.MULTIPLY_TOTAL, 1.25)
            .stateChunkAttr(CypherAttributes.RECOIL, AttributeOperator.MULTIPLY_TOTAL, 0.0)
            .stateChunkAttr(CypherAttributes.SPREAD, AttributeOperator.ADD, 20.0)
            .stateChunkAttr(CypherAttributes.GRAVITY_FACTOR, AttributeOperator.ADD, 0.03)
    }

    override fun <CypherBeforeInit> redirectPosDireServer(
        level: ServerLevel,
        invoker: Entity?,
        owner: Entity?,
        cypherEntity: CypherBeforeInit,
        strength: Int,
        pair: PosDirePair,
        index: Int
    ): PosDirePair where CypherBeforeInit : Entity, CypherBeforeInit : ICypherBeforeInit {
        val height = (16.0 + 8.0 * strength).coerceAtMost(128.0)
        val length = (16.0 + 8.0 * strength).coerceAtMost(128.0)
        val (start, direction) = pair
        if (invoker != null && direction != Vec3.ZERO) {
            val route = direction.normalize().scale(length)
            val hit = RayCastUtility.getProjectileHitResult(start, cypherEntity,
                { e -> e != invoker && e !is DedicatedCypherProjectile && e.canBeHitByProjectile() },
                route, level, MARGIN)
            var remote = start.add(route)
            if (hit.type != HitResult.Type.MISS) {
                remote = hit.location
            }
            // TODO use SPREAD as factor
            val h = Vec3(0.0, 1.0, 0.0).offsetRandom(invoker.random, 0.25f).scale(height)
            val pos = remote.add(h)
            return PosDirePair(pos, pos.vectorTo(hit.location))
        }
        return pair
    }

}