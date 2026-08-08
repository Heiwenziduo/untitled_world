package com.github.nahnullscience.cypher_nexus.content.cypher.modifier

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.mod.CypherSteerers.ESCORT_ORBIT_STEERER
import com.github.nahnullscience.cypher_nexus.init.mod.Cyphers.RANDOM_FIREWORK_ROCKET
import com.github.nahnullscience.cypher_nexus.init.mod.Cyphers.SNOWBALL
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap.Builder
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ModifierCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.spawnCypherEntityRaw
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.TickBehaviorHook
import com.github.nahnullscience.cypher_nexus.utility.PosDirePair
import net.minecraft.core.Holder
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level

abstract class AbstractEscortOrbit(
    defaultAttribute: Builder.() -> Builder
) : ModifierCypher(defaultAttribute), TickBehaviorHook {
    companion object {
        // count 1 gen 4, 2+ gen 8
        private fun Int.isTimeToGenerate(): Boolean {
            return when(this) {
                3, 7, 11, 15 -> true
                else -> false
            }
        }
    }

    protected abstract fun <CE> getProjectileToGen(cyEntity: CE): Holder<out AbstractProjectileCypher<*>>
            where CE : Entity, CE : ICypherEntity

    final override fun <CE> onTick(
        index: Int,
        count: Int,
        level: Level,
        cyEntity: CE
    ) where CE : Entity, CE : ICypherEntity {
        val level = cyEntity.level() as? ServerLevel ?: return
        if (cyEntity.tickCount.isTimeToGenerate()) { // "count doesn't count"
            spawnCypherEntityRaw(
                getProjectileToGen(cyEntity),
                level,
                ESCORT_ORBIT_STEERER,
                PosDirePair(cyEntity.position()),
                cyEntity
            )
        }
    }

    class SnowballOrbit(
        defaultAttribute: Builder.() -> Builder
    ) : AbstractEscortOrbit(defaultAttribute) {
        override val resource = CypherNexus.modResource("snowball_orbit")
        override fun <CE> getProjectileToGen(cyEntity: CE): Holder<out AbstractProjectileCypher<*>>
                where CE : Entity, CE : ICypherEntity = SNOWBALL
    }

    class FireworkOrbit(
        defaultAttribute: Builder.() -> Builder
    ) : AbstractEscortOrbit(defaultAttribute) {
        override val resource = CypherNexus.modResource("firework_orbit")
        override fun <CE> getProjectileToGen(cyEntity: CE): Holder<out AbstractProjectileCypher<*>>
                where CE : Entity, CE : ICypherEntity = RANDOM_FIREWORK_ROCKET
    }

    class JuxtaOrbit(
        defaultAttribute: Builder.() -> Builder
    ) : AbstractEscortOrbit(defaultAttribute) {
        override val resource = CypherNexus.modResource("juxta_orbit")
        override fun <CE> getProjectileToGen(cyEntity: CE): Holder<out AbstractProjectileCypher<*>>
                where CE : Entity, CE : ICypherEntity = cyEntity.cypherHolder
    }
}