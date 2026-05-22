package com.github.nahnullscience.cypher_nexus.content.cypher.modifier

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.content.entity.AbstractCypherProjectile
import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ModifierCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttributeOperation
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.invoking.HookInvokeRedirectPosServer
import com.github.nahnullscience.cypher_nexus.utility.RayCastUtility
import com.github.nahnullscience.cypher_nexus.utility.mod.PosDirePair
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3

object DaedalusCypher : ModifierCypher(
    manaDrain = 50f
), HookInvokeRedirectPosServer {
    const val MARGIN = 0.3f
    override val resource = CypherNexus.modResource("daedalus")
    init {
        addAttribute(CypherAttributes.SPEED, CypherAttributeOperation.MULTIPLY_TOTAL, 1.25)
    }
    override fun redirectPosDireServer(
        level: ServerLevel,
        invoker: Entity?,
        projectile: AbstractCypherProjectile,
        strength: Int,
        pair: PosDirePair,
        index: Int
    ): PosDirePair {
        val height = -8.0 + 16.0 * strength
        val length = 12.0 + 8.0 * strength
        val (start, direction) = pair
        if (invoker != null && direction != Vec3.ZERO) {
            val route = direction.normalize().scale(length)
            val hit = RayCastUtility.getProjectileHitResult(start, projectile,
                { e -> e != invoker && e !is AbstractCypherProjectile && e.canBeHitByProjectile() },
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