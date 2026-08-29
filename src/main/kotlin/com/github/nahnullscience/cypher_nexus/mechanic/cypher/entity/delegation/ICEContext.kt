package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntityLogicContext
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.steerer.AbstractCypherSteerer
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotState
import com.github.nahnullscience.cypher_nexus.utility.i.IDebug
import net.minecraft.world.entity.Entity

interface ICEContext <CE> : ICypherEntityLogicContext, IDebug where CE : Entity, CE : ICypherEntity {
    fun initCypher(cypher: AbstractProjectileCypher<*>, shotState: ShotState?, steerer: AbstractCypherSteerer?)
    fun initEntity(ce: CE)
}
