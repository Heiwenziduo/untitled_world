package com.github.nahnullscience.cypher_nexus.mechanic.cypher

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.DedicatedCypherProjectile
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation.ICypherEntity
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
    abstract val projectileType: Supplier<out EntityType<out CE>>

    protected open val builtinTrigger: TriggerType = TriggerType.NONE
    protected open val builtinTriggerCharge: Int = 1

    /**
     * @param trigger externally forced `TriggerType` override. use [builtinTrigger] if null.
     * @param charge only consulted when [trigger] is non-null; builtin path uses [builtinTriggerCharge].
     * @return the chunk subsequent draws in this invocation should populate.
     *
     * Note there should be at least one [draw] to make trigger function
     */
    open fun addCEToStateChunk(
        chunk: ShotStateChunk,
        trigger: TriggerType? = null,
        charge: Int = Int.MAX_VALUE,
    ): ShotStateChunk {
        val t = trigger ?: builtinTrigger
        if (t == TriggerType.NONE) {
            chunk.addProjectile(ProjectileNode(this, null))
            return chunk
        }
        val subState = ShotStateChunk(if (trigger != null) charge else builtinTriggerCharge)
        chunk.addProjectile(ProjectileNode(this, subState, t))
        return subState
    }

    open fun createProjectile(
        level: ServerLevel,
        invoker: Entity?,
        shootState: ShotStateChunk,
        node: ProjectileNode,
        stateHooks: HookContainer?
    ): CE {
        val proj = DedicatedCypherProjectile.create(
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