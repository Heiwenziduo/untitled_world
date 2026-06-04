package com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking

import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.init.mod.CypherBehaviorHooks
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractNonProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttributeOperation
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookContainer
import com.github.nahnullscience.cypher_nexus.utility.i.IFlaggable
import com.github.nahnullscience.cypher_nexus.utility.mod.CypherUtility
import com.github.nahnullscience.cypher_nexus.utility.mod.PosDirePair
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level

class ProjectileStateChunk private constructor (
    private var charge: Int,
    private val helper: InvokingHelper?
) : IFlaggable {
    /** normal chunk can only release once */
    constructor(charge: Int = 1): this(charge, null)
    companion object {
        fun root(helper: InvokingHelper) = ProjectileStateChunk(1, helper)
    }

    override var enabledFlags: Int = 0
    val computedOperationMap = HashMap<CypherAttribute, HashMap<CypherAttributeOperation, Double>>()

    private val hooks = HookContainer()
    private val modifiers = mutableListOf<ModifierNode>()
    private val projectiles = mutableListOf<ProjectileNode>()

    fun release(level: Level, directInvoker: Entity?, owner: Entity?, posDire: PosDirePair) {
        if (level.isClientSide) return
        if (charge-- <= 0) return

        // do recoil only on root
        if (directInvoker != null && directInvoker == owner && helper != null) {
            val recoilMap = computedOperationMap[CypherAttributes.RECOIL.value()]
            if (recoilMap != null) {
                var recoil = CypherUtility.attributeCalculator(recoilMap, 0.0)
                recoil = CypherAttributes.RECOIL.value().restrictRange(recoil)
                helper.wandInstance()?.recoilModule(directInvoker, recoil, posDire)
            }
        }

        for ((i, node) in projectiles.withIndex()) {

            val proj = node.instance.createProjectile(
                level,
                owner,
                posDire.position,
                posDire.direction,
                this,
                node,
                hooks)

            val newPair = hooks.cumulateHooks(CypherBehaviorHooks.INVOKE_REDIRECT_POS_SERVER, posDire)
            { h, l, pair -> h.redirectPosDireServer(level as ServerLevel, directInvoker, proj, l, pair, i) }

            proj.setDirection(newPair)
            level.addFreshEntity(proj)
        }
    }

    fun addProjectile(node: ProjectileNode): ProjectileStateChunk {
        projectiles.add(node)
        return this
    }

    fun attachHooks(cypher: AbstractNonProjectileCypher): ProjectileStateChunk {
        hooks.add(cypher)
        return this
    }

    fun enableFlags(flag: Int): ProjectileStateChunk {
        enableFlag(flag)
        return this
    }
}