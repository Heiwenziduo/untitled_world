package com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.init.mod.CypherHooks
import com.github.nahnullscience.cypher_nexus.init.mod.WandModuleTypes.RECOIL
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractNonProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.AttributeOperator
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.AttributeOperator.Companion.OperatorMap
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookContainer
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.ItemWandInstance
import com.github.nahnullscience.cypher_nexus.utility.centeredAABB
import com.github.nahnullscience.cypher_nexus.utility.i.IFlagExtension
import com.github.nahnullscience.cypher_nexus.utility.mod.MapOfCypherCounts
import com.github.nahnullscience.cypher_nexus.utility.mod.PosDirePair
import com.github.nahnullscience.cypher_nexus.utility.randomInCone
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
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
    /** normal chunk can only release once */
    constructor(charge: Int = 1): this(charge, null)
    constructor(ccMap: MapOfCypherCounts): this() { dirty = true; _ccMap = ccMap }

    companion object {
        private const val CAPTURE_RADIUS = 8.0
        private const val CAPTURE_RADIUS_HALF = CAPTURE_RADIUS / 2

        fun root(helper: InvokingHelper) = ShotStateChunk(1, helper)
    }

    val isRoot: Boolean by lazy { helper != null && helper.shotRoot == this }
    val accessor: ShotStateAccessor by lazy { ShotStateAccessor() }

    val attr2opMap: Reference2ObjectOpenHashMap<CypherAttribute, OperatorMap> by
    lazy { Reference2ObjectOpenHashMap() }

    val hooks: HookContainer by lazy { HookContainer() }

    private val simpleProjectiles: Reference2IntOpenHashMap<AbstractProjectileCypher<*>> by
    lazy { Reference2IntOpenHashMap() } // projectiles with no trigger

    private val triggeredProjectiles = mutableListOf<ProjectileNode>()
    val projectilesView get() = triggeredProjectiles.toList()

    private var _ccMap: MapOfCypherCounts? = null
    val ccMap: MapOfCypherCounts get() = _ccMap ?: MapOfCypherCounts().also { _ccMap = it }


    private var dirty = false
    override var enabledFlags: Int = 0

    var delay: Int = 0
    private set
    var recharge: Int = 0
    private set

    private fun spawnProjectile(
        cypher: AbstractProjectileCypher<*>,
        node: ProjectileNode?,
        hookedPosDire: PosDirePair,
        level: ServerLevel,
        owner: Entity?,
        directInvoker: Entity?
    ) {
        val proj = cypher.createProjectile(level, owner, this, node)
        var dire = hookedPosDire.direction
        run spread@ {
            val random = owner?.random ?: directInvoker?.random ?: return@spread
            val spreadMap = attr2opMap[CypherAttributes.SPREAD.value()] ?: return@spread

            val spread = AttributeOperator.attributeCalculator(CypherAttributes.SPREAD.value().defaultValue, spreadMap).let {
                CypherAttributes.SPREAD.value().restrictRange(it)
            }

            if (spread > 0.01)
                dire = hookedPosDire.direction.randomInCone(spread / 2, random)
//                        .also { println("dire: $dire -> $it, " +
//                                "actual scatter: ${Math.toDegrees(acos(dire.normalize().dot(it.normalize())))}, " +
//                                "spread: $spread") }
        }
        proj.initDirection(PosDirePair(hookedPosDire.position, dire))
        level.addFreshEntity(proj)
    }

    fun release(level: Level, directInvoker: Entity?, owner: Entity?, posDire: PosDirePair, itemWand: ItemWandInstance?) {
        if (charge-- <= 0) return

        Profiler.get().push { "cypherEntityCreation" }
        if (dirty) compute()
        CypherNexus.debugCypher { "${level.isClientSide} client ccMap: $_ccMap" }

        // do recoil only on root
        run recoil@ {
            itemWand ?: return@recoil
            directInvoker ?: return@recoil
            val recoilMap = attr2opMap[CypherAttributes.RECOIL.value()] ?: return@recoil
            val recoilModule = itemWand.module(RECOIL) ?: return@recoil

            val recoil = AttributeOperator.attributeCalculator(CypherAttributes.RECOIL.value().defaultValue, recoilMap).let {
                CypherAttributes.RECOIL.value().restrictRange(it)
            }

            recoilModule.recoil(directInvoker, recoil, posDire)
        }

        // handle entities only on server
        if (level !is ServerLevel) return

        // apply invoking redirection if hooked
        var hookedPosDire = posDire
        hooks[CypherHooks.INVOKE_POS_REDIRECTION_SERVER]?.let {
            hookedPosDire = hooks.cumulateHooks(
                CypherHooks.INVOKE_POS_REDIRECTION_SERVER,
                posDire
            ) { index, hook, count, cumulate ->
                hook.redirectPosDireServer(index, count, level, owner, accessor, directInvoker, cumulate)
            }
        }

        // capture surroundings if hooked
        hooks[CypherHooks.INVOKE_CAPTURE_SERVER]?.let {
            val pairCopy = hookedPosDire.copy()
            val entities = level.getEntities(null, pairCopy.position.centeredAABB(CAPTURE_RADIUS_HALF))
            entities.forEach { entity ->
                hooks.playHooks(CypherHooks.INVOKE_CAPTURE_SERVER) { index, hook, count ->
                    hook.forEntityCapturedServer(index, count, level, owner, accessor, pairCopy, entity)
                }
            }
        }

        // generate bullets
        simpleProjectiles.reference2IntEntrySet().forEach { (cypher, count) ->
            repeat(count) { spawnProjectile(cypher, null, hookedPosDire, level, owner, directInvoker) }
        }
        triggeredProjectiles.forEach { node ->
            spawnProjectile(node.instance, node, hookedPosDire, level, owner, directInvoker)
        }

        Profiler.get().pop()
    }

    fun addProjectileNode(
        cypher: AbstractProjectileCypher<*>,
        payload: ShotStateChunk,
        trigger: TriggerType
    ): ShotStateChunk = apply {
        dirty = true
        triggeredProjectiles.add(ProjectileNode(cypher, payload, trigger))
    }
    fun addProjectileNode(cypher: AbstractProjectileCypher<*>): ShotStateChunk = apply {
        dirty = true
        simpleProjectiles.addTo(cypher, 1)
    }

    fun attachHooks(cypher: AbstractNonProjectileCypher): ShotStateChunk = apply { hooks.add(cypher) }

    fun enableFlags(flag: Int): ShotStateChunk = apply { enableFlag(flag) }

    fun record(cy: AbstractCypher): ShotStateChunk = apply {
        dirty = true
        ccMap.count(cy)
    }

    fun compute(): ShotStateChunk {
        if (!dirty) return this

        _ccMap?.forEach { (cypher, counts) ->

            if (cypher is AbstractNonProjectileCypher) {
                enableFlag(cypher.flags)
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
                    targetChunk = helper!!.shotRoot
                }


                val chunkMap = targetChunk.attr2opMap.getOrPut(attribute) { EnumMap(AttributeOperator::class.java) }
                // prune: if set, skip
                if (chunkMap[AttributeOperator.SET_ALL] != null && cyMap[AttributeOperator.SET_ALL] == null) return@forEach

                cyMap.forEach { (operator, value) ->
                    chunkMap.compute(operator) { op, v ->
                        operator.cumulate(v ?: operator.defaultValue, value, counts)
                    }
                }
            }
        }

        dirty = false
        return this
    }

    /***/
    abstract inner class ShotStateViewer {
        fun getOpMap(attr: Holder<CypherAttribute>) = attr2opMap[attr.value()]

    }

    /***/
    inner class ShotStateAccessor : ShotStateViewer() {

        /**
         *
         * */
        fun addRaw(attr: Holder<CypherAttribute>, operator: AttributeOperator, value: Double) {
            val opMap = attr2opMap.getOrPut(attr.value()) { EnumMap(AttributeOperator::class.java) }
            opMap.compute(operator) { op, v ->
                operator.cumulate(v ?: operator.defaultValue, value)
            }
        }
    }
}