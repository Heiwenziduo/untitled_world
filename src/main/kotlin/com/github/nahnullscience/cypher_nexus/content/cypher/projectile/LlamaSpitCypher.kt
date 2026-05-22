package com.github.nahnullscience.cypher_nexus.content.cypher.projectile

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.content.entity.AbstractCypherProjectile
import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttributeOperation
import net.minecraft.world.level.Level

object LlamaSpitCypher : ProjectileCypher(
    manaDrain = 5f
) {
    override val resource = CypherNexus.modResource("llama_spit")

    init {
        addAttribute(CypherAttributes.RECHARGE_TIME, 2.0)

        addAttribute(CypherAttributes.DAMAGE, 1.0)
        addAttribute(CypherAttributes.SPEED, 1.0)
        addAttribute(CypherAttributes.EXISTING, 300.0)
        addAttribute(CypherAttributes.GRAVITY_FACTOR, 0.06)
        addAttribute(CypherAttributes.CRIT_CHANCE, CypherAttributeOperation.ADD,0.05)
    }

    override fun visualEffectOnHit(level: Level, projectile: AbstractCypherProjectile) {
        val pos = projectile.position()

    }
}