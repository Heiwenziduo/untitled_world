package com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher

data class ProjectileNode(
    val instance: AbstractProjectileCypher<*>,
    val payload: ShotState? = null,
    val trigger: TriggerType = TriggerType.NONE
)