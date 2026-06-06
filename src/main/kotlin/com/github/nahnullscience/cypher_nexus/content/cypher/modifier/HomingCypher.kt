package com.github.nahnullscience.cypher_nexus.content.cypher.modifier

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractCypherProjectile
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ModifierCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.BothTickBehaviorHook
import net.minecraft.world.level.Level

object HomingCypher: ModifierCypher(), BothTickBehaviorHook {
    override val resource = CypherNexus.modResource("homing")

    override fun defaultAttributes(): CypherDataMap.Builder {
        return super.defaultAttributes()
            .manaDrain(60f)
    }

    override fun tickBehaviorBoth(
        level: Level,
        projectile: AbstractCypherProjectile,
        strength: Int
    ) {
        // TODO
    }

}