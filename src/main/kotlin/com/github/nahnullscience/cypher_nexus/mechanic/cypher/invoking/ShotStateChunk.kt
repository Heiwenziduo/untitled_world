package com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking

import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.init.mod.CypherBehaviorHooks
import com.github.nahnullscience.cypher_nexus.init.mod.WandModuleTypes.RECOIL
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractNonProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.AttributeOperator
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookContainer
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.ItemWandInstance
import com.github.nahnullscience.cypher_nexus.utility.i.IFlagExtension
import com.github.nahnullscience.cypher_nexus.utility.mod.MapOfCypherCounts
import com.github.nahnullscience.cypher_nexus.utility.mod.PosDirePair
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import java.util.*

class ShotStateChunk private constructor (
    private var charge: Int,
    /** only root has access to the helper */
    private val helper: InvokingHelper?
) : IFlagExtension {
    companion object {
        fun root(helper: InvokingHelper) = ShotStateChunk(1, helper)
    }
    /** normal chunk can only release once */
    constructor(charge: Int = 1): this(charge, null)
    constructor(mocc: MapOfCypherCounts): this() {
        _countMap = mocc
    }


    private var _countMap = MapOfCypherCounts.of()
    private var dirty = true
    val ccMap get() = _countMap
    val isRoot: Boolean by lazy { helper != null && helper.rootChunk == this }

    override var enabledFlags: Int = 0

    val computedOperationMap = HashMap<CypherAttribute, EnumMap<AttributeOperator, Double>>()

    val hooks = HookContainer()
    private val projectiles = mutableListOf<ProjectileNode>()

    var delay: Int = 0
    private set
    var recharge: Int = 0
    private set

    fun release(level: Level, directInvoker: Entity?, owner: Entity?, posDire: PosDirePair, itemWand: ItemWandInstance?) {
        if (charge-- <= 0) return
        if (dirty) compute()

        println("${level.isClientSide} client mocc: $_countMap")
        // do recoil only on root
        run {
            itemWand ?: return@run
            directInvoker ?: return@run
            val recoilMap = computedOperationMap[CypherAttributes.RECOIL.value()] ?: return@run
            val recoilModule = itemWand.module(RECOIL) ?: return@run

            var recoil = AttributeOperator.attributeCalculator(recoilMap, 0.0)
            recoil = CypherAttributes.RECOIL.value().restrictRange(recoil)
            recoilModule.recoil(directInvoker, recoil, posDire)
        }

        // handle entities only on server
        if (level !is ServerLevel) return
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

            proj.initDirection(newPosPair)
            level.addFreshEntity(proj)
        }
    }

    fun addProjectile(node: ProjectileNode): ShotStateChunk {
        projectiles.add(node)
        return this
    }

    fun attachHooks(cypher: AbstractNonProjectileCypher): ShotStateChunk {
        hooks.add(cypher)
        return this
    }

    fun enableFlags(flag: Int): ShotStateChunk {
        enableFlag(flag)
        return this
    }

    fun record(cy: AbstractCypher): Int {
        dirty = true
        return _countMap.count(cy)
    }

    fun compute(): ShotStateChunk {
        if (!dirty) return this

        _countMap.forEach { (cypher, counts) ->

            if (cypher is AbstractNonProjectileCypher) {
                enableFlags(cypher.flags)
                hooks.add(cypher, counts)
            }

            // TODO cumulate from children
            if (isRoot) delay += cypher.delay * counts
            recharge += cypher.recharge * counts

            cypher.attributes().stateChunk.forEach { (attribute, cyMap) ->
                var targetChunk = this

                // FIXME cumulate from children
                if (isRoot &&
                    CypherAttributes.RECOIL.`is`(attribute.resource)
                    ) {
                    targetChunk = helper!!.rootChunk
                }


                val chunkMap = targetChunk.computedOperationMap.getOrPut(attribute)
                { EnumMap(AttributeOperator::class.java) }
                // prune: if set, skip
                if (chunkMap[AttributeOperator.SET_ALL] != null && cyMap[AttributeOperator.SET_ALL] == null) return@forEach

                cyMap.forEach { (operator, value) ->
                    if (operator != AttributeOperator.BASE) {
                        chunkMap.compute(operator) { op, v ->
                            operator.cumulate(v ?: operator.defaultValue, value, counts)
                        }
                    }
                }
            }
        }

        dirty = false
        return this
    }
}