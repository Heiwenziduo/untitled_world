package com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.init.mod.CypherHooks
import com.github.nahnullscience.cypher_nexus.init.mod.InvokingPatterns.NO_PATTERN
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractNonProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.AttributeFastMap
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.AttributeFastOperatorMap
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.AttributeFastOperatorMap.Companion.attrCalculator
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.AttributeOperator
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute.AttributeApply
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.spawnCypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookContainer
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.invoking.ServerInvokeAbortReleaseHook.ReleaseAbort
import com.github.nahnullscience.cypher_nexus.utility.centeredAABB
import com.github.nahnullscience.cypher_nexus.utility.i.IFlagExtension
import com.github.nahnullscience.cypher_nexus.utility.linear_space.AnchoredCoordinate
import com.github.nahnullscience.cypher_nexus.utility.linear_space.PosDirePair
import com.github.nahnullscience.cypher_nexus.utility.mod.MapOfCypherCounts
import com.github.nahnullscience.cypher_nexus.utility.randomInCone
import com.github.nahnullscience.cypher_nexus.utility.toVec3
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap
import net.minecraft.core.Holder
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.profiling.Profiler
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level

class ShotStateChunk private constructor (
    private var charge: Int,
    private val helper: InvokingHelper?
) : IFlagExtension {
    /**
     * create ShotState with given charge, this constructor will create a `payload` shot-state,
     * for creating root-state inside invoking process, see [root]
     * */
    constructor(charge: Int = 1): this(charge, null) {
        // give payloads a default spread
        attributes.setAttribute(CypherAttributes.SPREAD.value(), AttributeOperator.ADD, 60.0)
    }
    /**
     * init from ccMap, used by client side
     * */
    constructor(ccMap: MapOfCypherCounts): this() { dirty = true; _ccMapBacking = ccMap }

    companion object {
        private const val CAPTURE_RADIUS = 8.0
        private const val CAPTURE_RADIUS_HALF = CAPTURE_RADIUS / 2

        /**
         * create a `root` shot-state, only root has the access to `helper`
         * */
        fun root(helper: InvokingHelper) = ShotStateChunk(1, helper)
    }

//    private var _attrBacking: AttributeFastOperatorMap? = null
//    private val attributes: AttributeFastOperatorMap get() {
//        return _attrBacking ?: AttributeFastOperatorMap().also { _attrBacking = it }
//    }
    private val attributes = AttributeFastOperatorMap()

    private var _accessorBacking: ShotStateAccessor? = null
    val accessor: ShotStateAccessor get() {
        return _accessorBacking ?: ShotStateAccessor().also { _accessorBacking = it }
    }

    private var _hooksBacking: HookContainer? = null
    val hooks: HookContainer get() {
        return _hooksBacking ?: HookContainer().also { _hooksBacking = it }
    }

    private var _dyeAccBacking: DyeAccumulator? = null
    val dyeAccumulator: DyeAccumulator get() {
        return _dyeAccBacking ?: DyeAccumulator().also { _dyeAccBacking = it }
    }

    private var _ccMapBacking: MapOfCypherCounts? = null
    val ccMap: MapOfCypherCounts get() {
        return _ccMapBacking ?: MapOfCypherCounts().also { _ccMapBacking = it }
    }

    // projectiles with & without trigger
    private val simpleProjectiles: Reference2IntOpenHashMap<AbstractProjectileCypher<*>> = Reference2IntOpenHashMap(8)
    val simpleProjectilesView get() = simpleProjectiles.toMap()

    private val triggeredProjectiles = mutableListOf<ProjectileNode>()
    val triggeredProjectilesView get() = triggeredProjectiles.toList()


    val isRoot: Boolean get() = helper != null && helper.shotRoot == this

    var dirty = false
        private set
    var totalProjectiles: Int = 0
        private set

    override var enabledFlags: Int = 0

    private var shotPattern: Holder<AbstractInvokingPattern> = NO_PATTERN

    init {

    }

    fun release(
        level: Level,
        coordinate: AnchoredCoordinate,
        directInvoker: Entity?,
        owner: Entity?,
    ) {
        if (charge-- <= 0) return

        Profiler.get().push { "cypherEntityCreation" }
        if (dirty) compute()
        CypherNexus.debugCypher { "${level.isClientSide} client ccMap: $_ccMapBacking" }

        // handle entities only on server
        if (level !is ServerLevel) return

        // let hooks determine if this release should be aborted
        hooks[CypherHooks.INVOKE_ABORT_RELEASE_SERVER]?.let {
            hooks.playHooks(CypherHooks.INVOKE_ABORT_RELEASE_SERVER) { index, hook, count ->
                val result = hook.abortReleaseServer(index, count, level, owner, accessor)
                if (result == ReleaseAbort.ABORT) return
            }
        }

        // apply invoking redirection if hooked
        hooks[CypherHooks.INVOKE_REDIRECTION_SERVER]?.let {
            hooks.playHooks(CypherHooks.INVOKE_REDIRECTION_SERVER) { index, hook, count ->
                hook.invokeRedirectServer(index, count, level, owner, accessor, coordinate, directInvoker)
            }
        }

        // capture surroundings if hooked
        hooks[CypherHooks.INVOKE_CAPTURE_SERVER]?.let {
            val pos = coordinate.anchor.toVec3()
            val entities = level.getEntities(null, pos.centeredAABB(CAPTURE_RADIUS_HALF))
            entities.forEach { entity ->
                hooks.playHooks(CypherHooks.INVOKE_CAPTURE_SERVER) { index, hook, count ->
                    hook.forEntityCapturedServer(index, count, level, owner, accessor, coordinate, entity)
                }
            }
        }


        val spread = attributes.attrCalculator(CypherAttributes.SPREAD.value())

        // generate bullets
        simpleProjectiles.reference2IntEntrySet().forEach { (cypher, count) ->
            repeat(count) {
                wrapSpawn(cypher, null, coordinate, level, owner, directInvoker, spread)
            }
        }

        triggeredProjectiles.forEach { node ->
            wrapSpawn(node.instance, node, coordinate, level, owner, directInvoker, spread)
        }

        Profiler.get().pop()
    }

    private var indexP = 0
    /**
     * wrap [spawnCypherEntity] for pattern & chain effect supports
     * */
    private fun wrapSpawn(
        cypher: AbstractProjectileCypher<*>,
        node: ProjectileNode?,
        coordinate: AnchoredCoordinate,
        level: ServerLevel,
        owner: Entity?,
        directInvoker: Entity?,
        spread: Double
    ) {
        val patternPosDire = shotPattern.value().layout(indexP, totalProjectiles, coordinate)
        run layer@ {
            var dire = patternPosDire.direction
            run spread@ {
                val random = owner?.random ?: directInvoker?.random ?: return@spread
                if (spread > 0.01) dire = dire.randomInCone(spread / 2, random)
            }
            cypher.spawnCypherEntity(level, owner, this, node, PosDirePair(patternPosDire.position, dire))
        }
        indexP++
    }


    fun addProjectileNode(
        cypher: AbstractProjectileCypher<*>,
        payload: ShotStateChunk,
        trigger: TriggerType
    ): ShotStateChunk = apply {
        dirty = true
        triggeredProjectiles.add(ProjectileNode(cypher, payload, trigger))
    }
    fun addProjectileNode(cypher: AbstractProjectileCypher<*>, count: Int = 1): ShotStateChunk = apply {
        dirty = true
        simpleProjectiles.addTo(cypher, count.coerceAtLeast(0))
    }

    @Deprecated("state changes affected by this method won't be synced, use ccMap-friendly method instead")
    fun attachHooks(cypher: AbstractNonProjectileCypher): ShotStateChunk = apply { hooks.add(cypher) }

    @Deprecated("state changes affected by this method won't be synced, use ccMap-friendly method instead")
    fun enableFlags(flag: Int): ShotStateChunk = apply { enableFlag(flag) }

    /**
     * register cypher to [MapOfCypherCounts], this will make all shot-state attribute of the cypher into count.
     * this won't affect the projectile count.
     * @see addProjectileNode
     * */
    fun record(cy: AbstractCypher, count: Int = 1): ShotStateChunk = apply {
        dirty = true
        ccMap.count(cy, count.coerceAtLeast(0))
    }

    /** compute and lock the state */
    fun compute(): ShotStateChunk {
        if (!dirty) return this

        _ccMapBacking?.let { ccMap ->
            ccMap.forEach ccMap@ { (cypher, counts) ->
                // pattern
                cypher.pattern.let { if (it != NO_PATTERN) shotPattern = it }
                // hook
                hooks.add(cypher, counts)
                // flag & color (non-proj exclusive)
                if (cypher is AbstractNonProjectileCypher) {
                    enableFlag(cypher.flags)
                    run color@ {
                        cypher.rgb?.let { dyeAccumulator.addDye(it, counts) }
                        cypher.alpha.takeIf { it.isFinite() }?.let { dyeAccumulator.multiplyAlpha(it, counts) }
                        cypher.brightness.takeIf { it.isFinite() }?.let { dyeAccumulator.adjustBrightness(it, counts) }
                    }
                }
                // state-attributes
                attributes.absorb(cypher.dataMap().shotState, counts) { attr ->
                    attr.applyOn == AttributeApply.INVOKING_ROOT && !isRoot // abort if true
                }
            }

            dyeAccumulator.resolveColor()
            totalProjectiles = simpleProjectiles.values.sum() + triggeredProjectiles.size
        }

        dirty = false
        return this
    }

    fun computeAttribute(map: AttributeFastMap, cypher: AbstractProjectileCypher<*>) {
        attributes.forEach { (attr, values) ->
            if (!attr.isAttributeForEntity) return@forEach
            // TODO prune cumulation, some of attributes will not be used, depends on cypher implementation
            map.put(attr, attributes.attrCalculator(attr, cypher))
        }
    }

    fun computeRecoil(base: Double = 0.0): Double {
        val recoil = attributes.attrCalculator(CypherAttributes.RECOIL.value(), base)
        return recoil
    }


    /***/
    // TODO
    abstract inner class ShotStateViewer {
        fun getOpMap(attr: Holder<CypherAttribute>) = attributes[attr.value()]

    }

    /***/
    inner class ShotStateAccessor : ShotStateViewer() {

        /**
         *
         * */
        fun addRaw(attr: Holder<CypherAttribute>, operator: AttributeOperator, value: Double): Double {
            return attributes.cumulateAttribute(attr.value(), operator, value)
        }
    }
}