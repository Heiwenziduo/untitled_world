package com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking

import com.github.nahnullscience.cypher_nexus.init.mod.CypherBehaviorHookRegistry
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractNonProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttributeOperation
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookContainer
import com.github.nahnullscience.cypher_nexus.utility.i.IFlaggable
import com.github.nahnullscience.cypher_nexus.utility.mod.PosDirePair
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level

class ProjectileStateBlock() : IFlaggable {
    override var enabledFlags: Int = 0
    val computedOperationMap = HashMap<CypherAttribute, HashMap<CypherAttributeOperation, Double>>()

    private val hooks = HookContainer()
    private val modifiers = mutableListOf<ModifierNode>()
    private val projectiles = mutableListOf<ProjectileNode>()

    fun release(level: Level, invoker: Entity?, posDire: PosDirePair) {
        for ((i, p) in projectiles.withIndex()) {
            val proj = p.instance.createProjectile(level, invoker, posDire.position, posDire.direction, this, p.payload, hooks)

            val newPair = hooks.cumulateHooks(CypherBehaviorHookRegistry.INVOKE_REDIRECT_POS_SERVER, posDire)
            { h, l, pair -> h.redirectPosDireServer(level as ServerLevel, invoker, proj, l, pair, i) }

            proj.setDirection(newPair)
            level.addFreshEntity(proj)
        }
    }

    fun addProjectile(node: ProjectileNode): ProjectileStateBlock {
        projectiles.add(node)
        return this
    }

    fun attachHooks(cypher: AbstractNonProjectileCypher): ProjectileStateBlock {
        hooks.add(cypher)
        return this
    }

    fun enableFlags(flag: Int): ProjectileStateBlock {
        enableFlag(flag)
        return this
    }
}