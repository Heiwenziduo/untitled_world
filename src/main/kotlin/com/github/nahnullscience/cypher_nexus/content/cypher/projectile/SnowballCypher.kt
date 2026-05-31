package com.github.nahnullscience.cypher_nexus.content.cypher.projectile

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.content.entity.AbstractCypherProjectile
import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataAttach
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileStateChunk
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.level.Level

object SnowballCypher : ProjectileCypher(
    manaDrain = 3f
) {
    override val resource = CypherNexus.modResource("snowball")

    init {
        addAttribute(CypherAttributes.SPEED, 0.8)
        addAttribute(CypherAttributes.EXISTING, 300.0)
        addAttribute(CypherAttributes.GRAVITY_FACTOR, 0.03)
    }

    override fun defaultAttributes(): CypherDataAttach.Builder {
        return super.defaultAttributes()
            .manaDrain(3f)
            .projectileAttr(CypherAttributes.SPEED, 0.8)
            .projectileAttr(CypherAttributes.EXISTING, 300.0)
            .projectileAttr(CypherAttributes.GRAVITY_FACTOR, 0.03)
    }

    override fun invokeInHand(
        helper: InvokingHelper,
        chunk: ProjectileStateChunk,
        data: InvokingHelper.HelperDataBundle,
        state: InvokingHelper.HelperStateBundle,
        options: CypherInvokingOptions
    ) {
        super.invokeInHand(helper, chunk, data, state, options)
        println(attributes())
    }

    override fun visualEffectOnHit(level: Level, projectile: AbstractCypherProjectile) {
        val pos = projectile.position()
        // check: ItemParticleOption(ParticleTypes.ITEM, itemstack), and ParticleTypes.ITEM_SNOWBALL
        for (i in 0..7) {
            level.addParticle(ParticleTypes.ITEM_SNOWBALL, pos.x, pos.y, pos.z, 0.0, 0.0, 0.0)
        }
    }
}