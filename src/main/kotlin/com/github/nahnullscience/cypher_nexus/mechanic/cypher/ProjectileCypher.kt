package com.github.nahnullscience.cypher_nexus.mechanic.cypher

import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategories
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation.ICypherEntity
import net.minecraft.world.entity.Entity

abstract class ProjectileCypher <CY> : AbstractProjectileCypher<CY>() where CY : Entity, CY : ICypherEntity {

    final override val category = CypherCategories.PROJECTILE

}