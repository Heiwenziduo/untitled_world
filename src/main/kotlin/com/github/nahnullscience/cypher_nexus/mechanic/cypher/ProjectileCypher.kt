package com.github.nahnullscience.cypher_nexus.mechanic.cypher

import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategories
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation.ICypherEntity
import net.minecraft.world.entity.Entity

abstract class ProjectileCypher <CE> (
    defaultAttribute: CypherDataMap.Builder.() -> CypherDataMap.Builder = NONE_ATTR
): AbstractProjectileCypher <CE> (defaultAttribute) where CE : Entity, CE : ICypherEntity {

    final override val category = CypherCategories.PROJECTILE

}