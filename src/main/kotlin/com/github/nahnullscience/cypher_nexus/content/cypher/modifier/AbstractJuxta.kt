package com.github.nahnullscience.cypher_nexus.content.cypher.modifier

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.mod.CypherSteerers.NO_STEERER
import com.github.nahnullscience.cypher_nexus.init.mod.CypherSteerers.SLOW_BOOT_STEERER
import com.github.nahnullscience.cypher_nexus.init.mod.Cyphers
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap.Builder
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ModifierCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.spawnCypherEntityRaw
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.steerer.AbstractCypherSteerer
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.TickBehaviorHook
import com.github.nahnullscience.cypher_nexus.utility.PosDirePair
import com.github.nahnullscience.cypher_nexus.utility.randomInCone
import com.github.nahnullscience.cypher_nexus.utility.toVec3
import net.minecraft.core.Direction
import net.minecraft.core.Holder
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import kotlin.math.PI

/**
 * continuously generate `illusions` during lifetime
 *
 * an illusion is a raw, unmodified copy of projectile itself
 * */
abstract class AbstractJuxta(
    defaultAttribute: Builder.() -> Builder
) : ModifierCypher(defaultAttribute), TickBehaviorHook {
    companion object {

    }
    protected open val illusionSteerer: Holder<AbstractCypherSteerer> = NO_STEERER
    protected abstract fun <CE> shootingPosPair(cyEntity: CE): PosDirePair where CE : Entity, CE : ICypherEntity
    protected open fun <CE> isJuxtaTime(cyEntity: CE): Boolean where CE : Entity, CE : ICypherEntity {
        cyEntity.ccMap?.containsKey(Cyphers.PHANTOM_RUSH)?.let {
            if (it) return (cyEntity.tickCount - 1) and 3 == 3
        }
        return (cyEntity.tickCount - 1) and 7 == 7
    }

    final override fun <CE> onTick(
        index: Int,
        count: Int,
        level: Level,
        cyEntity: CE
    ) where CE : Entity, CE : ICypherEntity {
        val level = cyEntity.level() as? ServerLevel ?: return
        if (isJuxtaTime(cyEntity)) {
            spawnCypherEntityRaw(
                cyEntity.cypherHolder,
                level,
                illusionSteerer,
                shootingPosPair(cyEntity)
            )
        }
    }

    class PhantomRush(defaultAttribute: Builder.() -> Builder) : AbstractJuxta(defaultAttribute) {
        override val resource = CypherNexus.modResource("phantom_rush")
        override val illusionSteerer = SLOW_BOOT_STEERER
        override fun <CE> shootingPosPair(cyEntity: CE): PosDirePair where CE : Entity, CE : ICypherEntity {
            val dire: Vec3
            cyEntity.deltaMovement.let {
                dire = if (it == Vec3.ZERO) Vec3.ZERO else it.randomInCone(6.0, cyEntity.random)
            }
            return PosDirePair(cyEntity.position(), dire)
        }

        override fun <CE> isJuxtaTime(cyEntity: CE): Boolean where CE : Entity, CE : ICypherEntity {
            return (cyEntity.tickCount - 1) and 3 == 3
        }
    }

    class ChaoticJuxta(defaultAttribute: Builder.() -> Builder) : AbstractJuxta(defaultAttribute) {
        override val resource = CypherNexus.modResource("chaotic_juxta")
        override fun <CE> shootingPosPair(cyEntity: CE): PosDirePair where CE : Entity, CE : ICypherEntity {
            val dire = Vector3f(1f, 0f, 0f).randomInCone(180.0, cyEntity.random)
            return PosDirePair(cyEntity.position(), dire.toVec3())
        }
    }

    class DownwardJuxta(defaultAttribute: Builder.() -> Builder) : AbstractJuxta(defaultAttribute) {
        override val resource = CypherNexus.modResource("downward_juxta")
        override fun <CE> shootingPosPair(cyEntity: CE): PosDirePair where CE : Entity, CE : ICypherEntity {
            return PosDirePair(cyEntity.position(), Direction.DOWN.unitVec3)
        }
    }

    class UpwardJuxta(defaultAttribute: Builder.() -> Builder) : AbstractJuxta(defaultAttribute) {
        override val resource = CypherNexus.modResource("upward_juxta")
        override fun <CE> shootingPosPair(cyEntity: CE): PosDirePair where CE : Entity, CE : ICypherEntity {
            return PosDirePair(cyEntity.position(), Direction.UP.unitVec3)
        }
    }
}