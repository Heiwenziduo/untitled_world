package com.github.nahnullscience.cypher_nexus.mechanic.cypher

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractDedicatedCypherProjectile
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags.Companion.containsFlag
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookContainer
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileNode
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.TriggerType
import net.minecraft.core.Holder
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import java.util.function.Supplier

abstract class AbstractProjectileCypher <CE> (
    defaultAttribute: CypherDataMap.Builder.() -> CypherDataMap.Builder = NONE
) : AbstractCypher(defaultAttribute) where CE : Entity, CE : ICypherEntity {

    companion object {
        const val TRIGGER_CHARGE_MAX = 299_792_458 // large but finite, so decrements are traceable
    }

    abstract val projectileType: Supplier<out EntityType<out CE>>

    protected open val builtinTrigger: TriggerType = TriggerType.NONE
    protected open val builtinTriggerCharge: Int = 1

    /**
     * register cypher-entity(s) to [ShotStateChunk]
     * @param trigger externally forced `TriggerType` override. use [builtinTrigger] if null.
     * @param charge only consulted when [trigger] is non-null; builtin path uses [builtinTriggerCharge].
     * @return the shot-state subsequent draws in this invocation should populate.
     *
     * Note there should be at least one [draw] to make trigger function.
     */
    open fun addToShotState(
        shotState: ShotStateChunk,
        trigger: TriggerType? = null,
        charge: Int = TRIGGER_CHARGE_MAX,
    ): ShotStateChunk {
        val t = trigger ?: builtinTrigger
        if (t == TriggerType.NONE) {
            shotState.addProjectileNode(this)
            return shotState
        }
        val charge =
            if (trigger != null) charge
            else if ((flags and shotState.enabledFlags).containsFlag(CypherFlags.PIERCE_ENTITY)) TRIGGER_CHARGE_MAX
            else builtinTriggerCharge

        val subState = ShotStateChunk(charge)
        shotState.addProjectileNode(this, subState, t)
        return subState
    }

    open fun createProjectile(
        level: ServerLevel,
        invoker: Entity?,
        shootState: ShotStateChunk,
        node: ProjectileNode?,
    ): CE {
        val proj = AbstractDedicatedCypherProjectile.create(
            this,
            projectileType.get(),
            level,
            invoker,
            shootState,
            node,
        )
        return proj
    }

    fun getAttrBaseOrDefault(holder: Holder<CypherAttribute>) = getAttrBaseOrDefault(holder.value())
    fun getAttrBaseOrDefault(attr: CypherAttribute): Double = attributes().projectile[attr] ?: attr.defaultValue
    fun getAttrBaseOrNull(holder: Holder<CypherAttribute>) = getAttrBaseOrNull(holder.value())
    fun getAttrBaseOrNull(attr: CypherAttribute): Double? = attributes().projectile[attr]

    override fun triggerInterplay() = true
}