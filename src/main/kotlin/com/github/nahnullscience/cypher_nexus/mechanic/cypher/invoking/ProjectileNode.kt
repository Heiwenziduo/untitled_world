package com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher

data class ProjectileNode(
    val instance: AbstractProjectileCypher,
    val payload: ProjectileStateChunk? = null,
    val trigger: TriggerType = TriggerType.NONE
)