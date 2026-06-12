package com.github.nahnullscience.cypher_nexus.content.cypher.utility

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractCypherProjectile
import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategories
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractNonProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.invoking.ServerInvokeRedirectPosHook
import com.github.nahnullscience.cypher_nexus.utility.mod.PosDirePair
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity

object InnerForceCypher : AbstractNonProjectileCypher(), ServerInvokeRedirectPosHook {
    override val resource = CypherNexus.modResource("inner_force")
    override val category = CypherCategories.UTILITY
    override fun defaultAttributes(): CypherDataMap.Builder {
        return super.defaultAttributes()
            .manaDrain(10f)
            .draw(1)
    }

    override fun redirectPosDireServer(
        level: ServerLevel,
        invoker: Entity?,
        owner: Entity?,
        projectile: AbstractCypherProjectile,
        strength: Int,
        pair: PosDirePair,
        index: Int
    ): PosDirePair {
        owner?: return pair
        return PosDirePair(owner.eyePosition, owner.eyePosition.vectorTo(pair.position))
    }
}