package com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.init.mod.CypherHooks
import com.github.nahnullscience.cypher_nexus.init.mod.InvokingPatterns.NO_PATTERN
import com.github.nahnullscience.cypher_nexus.init.mod.WandModuleTypes.RECOIL_MODULE
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractNonProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.AttributeOperator
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute.AttributeApply
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.spawnCypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookContainer
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.invoking.ServerInvokeAbortReleaseHook.ReleaseAbort
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.ItemWandInstance
import com.github.nahnullscience.cypher_nexus.utility.CoordinateDefinition
import com.github.nahnullscience.cypher_nexus.utility.centeredAABB
import com.github.nahnullscience.cypher_nexus.utility.i.IFlagExtension
import com.github.nahnullscience.cypher_nexus.utility.mod.AttributeFastOpMap
import com.github.nahnullscience.cypher_nexus.utility.mod.MapOfCypherCounts
import com.github.nahnullscience.cypher_nexus.utility.PosDirePair
import com.github.nahnullscience.cypher_nexus.utility.randomInCone
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap
import net.minecraft.core.Holder
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.profiling.Profiler
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.Level
import java.util.*

class ShotStateChunk private constructor (
    private var charge: Int,
    /** only root has access to the helper */
    private val helper: InvokingHelper?
) : IFlagExtension {
    /** normal chunk can only release once */
    constructor(charge: Int = 1): this(charge, null)
    constructor(ccMap: MapOfCypherCounts): this() { dirty = true; _ccMapBacking = ccMap }

    companion object {
        private const val CAPTURE_RADIUS = 8.0
        private const val CAPTURE_RADIUS_HALF = CAPTURE_RADIUS / 2

        fun root(helper: InvokingHelper) = ShotStateChunk(1, helper)
    }

    private var _accessorBacking: ShotStateAccessor? = null
    val accessor: ShotStateAccessor get() {
        return _accessorBacking ?: ShotStateAccessor().also { _accessorBacking = it }
    }

    private var _attrBacking: AttributeFastOpMap? = null
    val attributes: AttributeFastOpMap get() {
        return _attrBacking ?: AttributeFastOpMap().also { _attrBacking = it }
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


    fun release(
        level: Level,
        invokerCoordinate: CoordinateDefinition,
        posDire: PosDirePair,
        directInvoker: Entity?,
        owner: Entity?,
        wandInstance: ItemWandInstance?
    ) {
        if (charge-- <= 0) return

        Profiler.get().push { "cypherEntityCreation" }
        if (dirty) compute()
        CypherNexus.debugCypher { "${level.isClientSide} client ccMap: $_ccMapBacking" }

        // do recoil only on root
        if (isRoot) run recoil@ {
            wandInstance ?: return@recoil
            if (directInvoker !is LivingEntity) return@recoil
            val recoilMap = attributes[CypherAttributes.RECOIL.value()] ?: return@recoil
            val recoil = AttributeOperator.attributeCalculator(CypherAttributes.RECOIL.value().defaultValue, recoilMap).let {
                CypherAttributes.RECOIL.value().restrictRange(it)
            }
            wandInstance.functionModule(RECOIL_MODULE.get(), directInvoker, null, invokerCoordinate, power = recoil)
        }

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
            repeat(count) {
                spawnOne(cypher, null, invokerCoordinate, hookedPosDire, level, owner, directInvoker)
            }
        }
        triggeredProjectiles.forEach { node ->
            spawnOne(node.instance, node, invokerCoordinate, hookedPosDire, level, owner, directInvoker)
        }

        Profiler.get().pop()
    }

    private var indexP = 0
    /**
     * wrap [spawnProjectile] for pattern & chain effect supports
     * */
    private fun spawnOne(
        cypher: AbstractProjectileCypher<*>,
        node: ProjectileNode?,
        coordinate: CoordinateDefinition,
        hookedPosDire: PosDirePair,
        level: ServerLevel,
        owner: Entity?,
        directInvoker: Entity?
    ) {
        val patternPosDire = shotPattern.value().layout(indexP, totalProjectiles, coordinate, hookedPosDire)
        spawnProjectile(indexP, cypher, node, patternPosDire, level, owner, directInvoker)
        indexP++
    }

    private fun spawnProjectile(
        index: Int,
        cypher: AbstractProjectileCypher<*>,
        node: ProjectileNode?,
        posDire: PosDirePair,
        level: ServerLevel,
        owner: Entity?,
        directInvoker: Entity?
    ) {
        var dire = posDire.direction
        run spread@ {
            val random = owner?.random ?: directInvoker?.random ?: return@spread
            val spreadMap = attributes[CypherAttributes.SPREAD.value()] ?: return@spread

            val spread = AttributeOperator.attributeCalculator(CypherAttributes.SPREAD.value().defaultValue, spreadMap).let {
                CypherAttributes.SPREAD.value().restrictRange(it)
            }

            if (spread > 0.01) dire = dire.randomInCone(spread / 2, random)
        }
        cypher.spawnCypherEntity(level, owner, this, node, PosDirePair(posDire.position, dire))
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
                cypher.dataMap().shotState.forEach cypherEach@ { (attribute, cyShotStateModifiers) ->

                    if (attribute.applyOn == AttributeApply.INVOKING_ROOT && !isRoot) return@cypherEach

                    val chunkMap = attributes.getOrPut(attribute) { EnumMap(AttributeOperator::class.java) }
                    // prune: if set, skip
                    if (chunkMap[AttributeOperator.SET_ALL] != null && cyShotStateModifiers[AttributeOperator.SET_ALL] == null) return@cypherEach

                    cyShotStateModifiers.forEach opMap@ { (operator, value) ->
                        chunkMap.compute(operator) { key, old ->
                            operator.cumulate(old ?: operator.defaultValue, value, counts)
                        }
                    }
                }
            }

            dyeAccumulator.resolveColor()
            totalProjectiles = simpleProjectiles.values.sum() + triggeredProjectiles.size
        }

        dirty = false
        return this
    }


    /***/
    abstract inner class ShotStateViewer {
        fun getOpMap(attr: Holder<CypherAttribute>) = attributes[attr.value()]

    }

    /***/
    inner class ShotStateAccessor : ShotStateViewer() {

        /**
         *
         * */
        fun addRaw(attr: Holder<CypherAttribute>, operator: AttributeOperator, value: Double) {
            val opMap = attributes.getOrPut(attr.value()) { EnumMap(AttributeOperator::class.java) }
            opMap.compute(operator) { op, v ->
                operator.cumulate(v ?: operator.defaultValue, value)
            }
        }
    }
}