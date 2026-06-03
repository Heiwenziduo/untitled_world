package com.github.nahnullscience.cypher_nexus.content.cypher.projectile

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.content.entity.AbstractCypherProjectile
import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ProjectileCypher
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.level.Level

object SnowballCypher : ProjectileCypher() {
    override val resource = CypherNexus.modResource("snowball")

    override fun defaultAttributes(): CypherDataMap.Builder {
        return super.defaultAttributes()
            .manaDrain(3f)
            .projectileAttr(CypherAttributes.SPEED, 0.8)
            .projectileAttr(CypherAttributes.EXISTING, 300.0)
            .projectileAttr(CypherAttributes.GRAVITY_FACTOR, 0.03)
    }


    override fun visualEffectOnHit(level: Level, projectile: AbstractCypherProjectile) {
        val pos = projectile.position()
        // check: ItemParticleOption(ParticleTypes.ITEM, itemstack), and ParticleTypes.ITEM_SNOWBALL
        for (i in 0..7) {
            level.addParticle(ParticleTypes.ITEM_SNOWBALL, pos.x, pos.y, pos.z, 0.0, 0.0, 0.0)
        }
    }
}