package com.github.nahnullscience.cypher_nexus.content.cypher.modifier

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ModifierCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractCypherProjectile
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.BothTickBehaviorHook
import net.minecraft.world.level.Level
import kotlin.math.min

object BoomerangCypher: ModifierCypher(), BothTickBehaviorHook {
    const val BOOMERANG_STRENGTH = 0.08
    override val resource = CypherNexus.modResource("boomerang")

    override fun defaultAttributes(): CypherDataMap.Builder {
        return super.defaultAttributes()
            .manaDrain(0f)
            .flags(CypherFlags.MOTION_FOLLOWS_OWNER)
    }

    override fun tickBehaviorBoth(
        level: Level,
        projectile: AbstractCypherProjectile,
        strength: Int
    ) {
        val target = projectile.owner() ?: return
        if (!target.boundingBox.contains(projectile.position())) {
            val dir = projectile.position().vectorTo(target.eyePosition)
            val dis =  min(dir.length(), strength * BOOMERANG_STRENGTH)
            val speed = dir.normalize().scale(dis)
            projectile.push(speed)
        }
    }
}