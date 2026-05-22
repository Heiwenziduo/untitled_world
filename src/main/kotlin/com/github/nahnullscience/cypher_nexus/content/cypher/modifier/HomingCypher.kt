package com.github.nahnullscience.cypher_nexus.content.cypher.modifier

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.content.entity.AbstractCypherProjectile
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ModifierCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.HookTickBehaviorBoth
import net.minecraft.world.level.Level

object HomingCypher: ModifierCypher(
    manaDrain = 50f
), HookTickBehaviorBoth {
    override val resource = CypherNexus.modResource("homing")
    override fun tickBehaviorBoth(
        level: Level,
        projectile: AbstractCypherProjectile,
        strength: Int
    ) {
        // TODO
    }

    init {

    }


}