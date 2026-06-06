package com.github.nahnullscience.cypher_nexus.content.cypher.static_projectile

import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.StaticProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.BothBeforeDiscardHook
import net.minecraft.world.level.ExplosionDamageCalculator

abstract class AbstractStaticSummonerCypher() : StaticProjectileCypher(), BothBeforeDiscardHook {

    override fun defaultAttributes(): CypherDataMap.Builder {
        return super.defaultAttributes()
            .flags(CypherFlags.PIERCE_ENTITY)
            .flags(CypherFlags.CONSTANT_EXISTING)
            .projectileAttr(CypherAttributes.EXISTING, 2.0)
    }

    companion object {
        // TODO
        val EXPLOSION_DAMAGE_CALCULATOR: ExplosionDamageCalculator = ExplosionDamageCalculator()

    }
}