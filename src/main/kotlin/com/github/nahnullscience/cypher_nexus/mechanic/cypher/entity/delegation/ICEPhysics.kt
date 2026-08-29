package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntityPhysics
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileNode
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotState
import com.github.nahnullscience.cypher_nexus.utility.i.IDebug
import net.minecraft.world.entity.Entity

interface ICEPhysics <CE> : ICypherEntityPhysics, IDebug where CE : Entity, CE : ICypherEntity {
    fun initCypher(cypher: AbstractProjectileCypher<*>, shotState: ShotState?, node: ProjectileNode?)
    fun initEntity(ce: CE)
}
