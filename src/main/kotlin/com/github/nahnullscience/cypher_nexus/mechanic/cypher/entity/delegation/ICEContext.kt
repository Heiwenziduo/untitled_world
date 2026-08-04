package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntityLogicContext
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.steerer.AbstractCypherSteerer
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk
import net.minecraft.world.entity.Entity

interface ICEContext <CE> : ICypherEntityLogicContext where CE : Entity, CE : ICypherEntity {
    fun initCypher(cypher: AbstractProjectileCypher<*>, shotState: ShotStateChunk?, steerer: AbstractCypherSteerer?)
    fun initEntity(ce: CE)
}