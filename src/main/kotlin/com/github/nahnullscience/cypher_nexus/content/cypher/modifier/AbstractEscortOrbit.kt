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
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.TickBehaviorHook
import net.minecraft.core.Holder
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3

abstract class AbstractEscortOrbit(
    defaultAttribute: Builder.() -> Builder
) : ModifierCypher(defaultAttribute), TickBehaviorHook {

    protected abstract fun <CE> getProjectileToGen(cyEntity: CE): Holder<out AbstractProjectileCypher<*>>
            where CE : Entity, CE : ICypherEntity

    protected open fun <CE> isTimeToGenerate(cyEntity: CE): Boolean where CE : Entity, CE : ICypherEntity {
        return when (cyEntity.tickCount - 1) {
            3, 7, 11, 15 -> true
            else -> false
        }
    }

    final override fun <CE> onTick(
        index: Int,
        count: Int,
        level: Level,
        cyEntity: CE
    ) where CE : Entity, CE : ICypherEntity {
        if (level is ServerLevel && isTimeToGenerate(cyEntity)) {
            spawnCypherEntityRaw(
                getProjectileToGen(cyEntity),
                level,
                ESCORT_ORBIT_STEERER,
                cyEntity,
                cyEntity.position(),
                Vec3.ZERO
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

        override fun <CE> isTimeToGenerate(cyEntity: CE): Boolean where CE : Entity, CE : ICypherEntity {
            if (cyEntity.hasFlag(CypherFlags.PHANTOM)) when (cyEntity.tickCount - 1) {
                3, 7, 11, 15 -> return true
            }
            else when (cyEntity.tickCount - 1) {
                3, 11 -> return true
            }
            return false
        }
    }
}
