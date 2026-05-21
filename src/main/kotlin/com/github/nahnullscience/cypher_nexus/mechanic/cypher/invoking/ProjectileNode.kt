package com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher

data class ProjectileNode(
    val instance: AbstractProjectileCypher,
    var payload: ProjectileStateBlock? = null
)