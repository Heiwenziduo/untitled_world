package com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking

import com.github.nahnullscience.cypher_nexus.CypherNexus
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
import com.github.nahnullscience.cypher_nexus.utility.randomInCone
import net.minecraft.core.Holder
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.profiling.Profiler
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
    constructor(ccMap: MapOfCypherCounts): this() {
        _countMap = ccMap
    }


    private var _countMap = MapOfCypherCounts.of()
    private var dirty = true
    val ccMap get() = _countMap
    val isRoot: Boolean by lazy { helper != null && helper.rootChunk == this }

    override var enabledFlags: Int = 0

    val computedOperationMap = HashMap<CypherAttribute, EnumMap<AttributeOperator, Double>>()

    val hooks = HookContainer()
    private val projectiles = mutableListOf<ProjectileNode>()
    val projectilesView get() = projectiles.toList()

    var delay: Int = 0
    private set
    var recharge: Int = 0
    private set

    fun release(level: Level, directInvoker: Entity?, owner: Entity?, posDire: PosDirePair, itemWand: ItemWandInstance?) {
        if (charge-- <= 0) return

        Profiler.get().push { "cypherEntityCreation" }
        if (dirty) compute()
        CypherNexus.debugCypher { "${level.isClientSide} client ccMap: $_countMap" }

        // do recoil only on root
        run recoil@ {
            itemWand ?: return@recoil
            directInvoker ?: return@recoil
            val recoilMap = computedOperationMap[CypherAttributes.RECOIL.value()] ?: return@recoil
            val recoilModule = itemWand.module(RECOIL) ?: return@recoil

            var recoil = AttributeOperator.attributeCalculator(recoilMap, CypherAttributes.RECOIL.value().defaultValue)
            recoil = CypherAttributes.RECOIL.value().restrictRange(recoil)
            recoilModule.recoil(directInvoker, recoil, posDire)
        }

        // handle entities only on server
        if (level !is ServerLevel) return

        // apply invoking redirection
        val hookedPosDire = hooks.cumulateHooks(
            CypherBehaviorHooks.INVOKE_REDIRECT_POS_SERVER,
            posDire
        ) { h, l, pair ->
            h.redirectPosDireServer(level, directInvoker, owner, l, pair, 0)
        }

        for ((i, node) in projectiles.withIndex()) {
            val proj = node.instance.createProjectile(
                level,
                owner,
                this,
                node,
                hooks
            )

            var dire = hookedPosDire.direction
            run spread@ {
                val random = owner?.random ?: directInvoker?.random ?: return@spread
                val spreadMap = computedOperationMap[CypherAttributes.SPREAD.value()] ?: return@spread

                val spread = AttributeOperator.attributeCalculator(spreadMap, CypherAttributes.SPREAD.value().defaultValue)
                    .let { CypherAttributes.SPREAD.value().restrictRange(it) }
                if (spread > 0.01)
                    dire = hookedPosDire.direction.randomInCone(spread / 2, random)
//                        .also { println("dire: $dire -> $it, " +
//                                "actual scatter: ${Math.toDegrees(acos(dire.normalize().dot(it.normalize())))}, " +
//                                "spread: $spread") }
            }

            // TODO consider let PosPair mutable, create too much
            proj.initDirection(PosDirePair(hookedPosDire.position, dire))

            Profiler.get().pop()
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

    inner class StateAttributeAccessor {
        fun getAttribute(attr: Holder<CypherAttribute>) = computedOperationMap[attr.value()]
    }
}