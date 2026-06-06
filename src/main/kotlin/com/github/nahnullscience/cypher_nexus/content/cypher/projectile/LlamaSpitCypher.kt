package com.github.nahnullscience.cypher_nexus.content.cypher.projectile

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.ModEntities
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractCypherProjectile
import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttributeOperation
import net.minecraft.world.level.Level

object LlamaSpitCypher : ProjectileCypher() {
    override val resource = CypherNexus.modResource("llama_spit")
    override val projectileType = ModEntities.CYPHER_LLAMA_SPIT

    override fun defaultAttributes(): CypherDataMap.Builder {
        return super.defaultAttributes()
            .manaDrain(5f)
            .recharge(1)
            .projectileAttr(CypherAttributes.DAMAGE, 1.0)
            .projectileAttr(CypherAttributes.SPEED, 1.0)
            .projectileAttr(CypherAttributes.EXISTING, 300.0)
            .projectileAttr(CypherAttributes.GRAVITY_FACTOR, 0.06)
            .stateChunkAttr(CypherAttributes.CRIT_CHANCE, CypherAttributeOperation.ADD, 0.05)
    }

//    override fun visualEffectOnHit(level: Level, projectile: AbstractCypherProjectile) {
//        val pos = projectile.position()
//
//    }
}