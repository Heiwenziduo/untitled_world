package com.github.nahnullscience.cypher_nexus.mechanic.cypher

import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategories
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity
import net.minecraft.world.entity.Entity

abstract class StaticProjectileCypher <CE> (
    defaultAttribute: CypherDataMap.Builder.() -> CypherDataMap.Builder = UNMODIFIED
): AbstractProjectileCypher <CE> (defaultAttribute) where CE : Entity, CE : ICypherEntity {

    final override val category = CypherCategories.STATIC_PROJECTILE

}