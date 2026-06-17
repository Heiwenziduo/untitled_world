package com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking

import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.init.mod.CypherBehaviorHooks
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractNonProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.AttributeOperator
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookContainer
import com.github.nahnullscience.cypher_nexus.utility.i.IFlaggable
import com.github.nahnullscience.cypher_nexus.utility.mod.MapOfCypherCounts
import com.github.nahnullscience.cypher_nexus.utility.mod.PosDirePair
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import java.util.*
import kotlin.collections.component1
import kotlin.collections.component2

class ProjectileStateChunk private constructor (
    private var charge: Int,
    /** only root has access to the helper */
    private val helper: InvokingHelper?
) : IFlaggable {
    companion object {
        fun root(helper: InvokingHelper) = ProjectileStateChunk(1, helper)
    }
    /** normal chunk can only release once */
    constructor(charge: Int = 1): this(charge, null)
    constructor(mocc: MapOfCypherCounts): this() {
        _countMap = mocc
    }


    private var _countMap = MapOfCypherCounts.of()
    val cyphers get() = _countMap

    override var enabledFlags: Int = 0

    val computedOperationMap = HashMap<CypherAttribute, EnumMap<AttributeOperator, Double>>()

    val hooks = HookContainer()
    private val projectiles = mutableListOf<ProjectileNode>()

    fun release(level: Level, directInvoker: Entity?, owner: Entity?, posDire: PosDirePair) {
        println("mocc: $_countMap")
        // do recoil only on root
        if (directInvoker != null && directInvoker == owner && helper != null) {
            val recoilMap = computedOperationMap[CypherAttributes.RECOIL.value()]
            if (recoilMap != null) {
                var recoil = AttributeOperator.attributeCalculator(recoilMap, 0.0)
                recoil = CypherAttributes.RECOIL.value().restrictRange(recoil)
                helper.wandInstance()?.recoilModule(directInvoker, recoil, posDire)
            }
        }

        // handle entities only on server
        if (level !is ServerLevel) return
        if (charge-- <= 0) return
        for ((i, node) in projectiles.withIndex()) {

            val proj = node.instance.createProjectile(
                level,
                owner,
                posDire.position,
                posDire.direction,
                this,
                node,
                hooks)

            val newPosPair = hooks.cumulateHooks(CypherBehaviorHooks.INVOKE_REDIRECT_POS_SERVER, posDire)
            { h, l, pair -> h.redirectPosDireServer(level, directInvoker, owner, proj, l, pair, i) }

            proj.setDirection(newPosPair)
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

    fun record(cy: AbstractCypher): Int {
        val i = _countMap[cy]
        _countMap[cy] = i + 1
        return i
    }

    fun compute(): ProjectileStateChunk {
        _countMap.forEach { (cypher, i) ->

            if (cypher is AbstractNonProjectileCypher) {
                enableFlags(cypher.flags)
                hooks.add(cypher, i)
            }

//            if (helper != null) helper.data.delay += cypher.delay
//            helper.data.recharge += cypher.recharge

            cypher.attributes().stateChunk.forEach { (attribute, cyMap) ->
//                var targetChunk = this
//                if (RECOIL.`is`(attribute.resource)) targetChunk = helper.rootChunk

                val chunkMap = computedOperationMap.getOrPut(attribute) { EnumMap(AttributeOperator::class.java) }
                if (chunkMap[AttributeOperator.SET_ALL] != null && cyMap[AttributeOperator.SET_ALL] == null) return@forEach

                cyMap.forEach { (operator, value) ->
                    if (operator != AttributeOperator.BASE) {
                        chunkMap.compute(operator) { op, v ->
                            operator.cumulate(v?: operator.defaultValue, value, i)
                        }
                    }
                }
            }
        }
        return this
    }
}