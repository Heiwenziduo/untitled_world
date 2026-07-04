package com.github.nahnullscience.cypher_nexus.content.cypher.projectile

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.ModEntities
import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.DedicatedCypherProjectile
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileNode
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.TriggerType

object SpawnEggCypher : ProjectileCypher<DedicatedCypherProjectile>() {
    override val resource = CypherNexus.modResource("spawn_egg")
    override val projectileType = ModEntities.CYPHER_SPAWN_EGG

    override fun defaultAttributes(): CypherDataMap.Builder {
        return super.defaultAttributes()
            .manaDrain(20f)
            .draw(1)
            .flags(CypherFlags.LINGER)
            .projectileAttr(CypherAttributes.SPEED, 1.0)
            .projectileAttr(CypherAttributes.EXISTING, 300.0)
            .projectileAttr(CypherAttributes.GRAVITY_FACTOR, 0.03)
    }

    override fun addToStateChunk(chunk: ShotStateChunk): ShotStateChunk {
        val subState = ShotStateChunk()
        chunk.addProjectile(ProjectileNode(this, subState, TriggerType.COLLISION))
        return subState
    }
}