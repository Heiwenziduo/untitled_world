package com.github.nahnullscience.cypher_nexus.content.cypher.modifier

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ModifierCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.DedicatedCypherProjectile
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.BothTickBehaviorHook
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import kotlin.math.min

object BoomerangCypher: ModifierCypher(), BothTickBehaviorHook {
    const val BOOMERANG_STRENGTH = 0.08
    override val resource = CypherNexus.modResource("boomerang")

    override fun defaultAttributes(): CypherDataMap.Builder {
        return super.defaultAttributes()
            .manaDrain(10f)
            .flags(CypherFlags.MOTION_FOLLOWS_OWNER)
    }

    override fun <CY> tickBehaviorBoth(
        level: Level,
        projectile: CY,
        strength: Int
    ) where CY : Entity, CY : ICypherEntity {
        val target = projectile.owner ?: return
        if (!target.boundingBox.contains(projectile.position())) {
            val dir = projectile.position().vectorTo(target.eyePosition)
            val dis =  min(dir.length(), strength * BOOMERANG_STRENGTH)
            val speed = dir.normalize().scale(dis)
            projectile.addDeltaMovement(speed)
        }
    }
}