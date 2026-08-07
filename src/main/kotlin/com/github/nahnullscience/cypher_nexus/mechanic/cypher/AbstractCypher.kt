package com.github.nahnullscience.cypher_nexus.mechanic.cypher

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.data_driven.ModDataMaps.CYPHER_DATA_ATTACH
import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.init.mod.CypherHooks
import com.github.nahnullscience.cypher_nexus.init.mod.Cyphers
import com.github.nahnullscience.cypher_nexus.init.mod.InvokingPatterns.NO_PATTERN
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap.Builder
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherProperties.*
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.AttributeOperator
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.category.CypherCategory
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookModule
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookModule.HookType
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.AbstractInvokingPattern
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.HelperDataBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.InvokingParameterBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk
import com.github.nahnullscience.cypher_nexus.utility.i.IRegisterable
import net.minecraft.ChatFormatting
import net.minecraft.core.Holder
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerPlayer

/**
 *
 * */
sealed class AbstractCypher(
    protected val defaultAttribute: Builder.() -> Builder = NONE_ATTR
): IRegisterable {
    companion object {
        val NONE_ATTR: Builder.() -> Builder = { this }
    }

    abstract val category: Holder<CypherCategory>

    val manaDrain: Float get() = dataMap().manaDrain
    val draw: Int get() = dataMap().draw
    val delay: Int get() = dataMap().delay
    val recharge: Int get() = dataMap().recharge
    val flags: Int get() = dataMap().flags

    open val pattern: Holder<AbstractInvokingPattern> = NO_PATTERN

    /** override colors from category */
    open val overrideBorder: Boolean = false
    open val borderColor: Int = 0
    /** whether the cypher shows in the index(left side) */
    open val hide: Boolean = false

    @Volatile // for future threaded access
    private var _implementHooksBacking: List<HookModule<*>>? = null
    /** auto-detect hooks */
    val implementedHooks: List<HookModule<*>>
        get() {
            val local = _implementHooksBacking
            return local ?:
            CypherHooks.REGISTRY.filter { module ->
                val c1 = module.hook.isInstance(this)
                val c2 = (this is AbstractProjectileCypher<*> && module.type == HookType.BEHAVIOR)
                    .also { if (c1 && it) CypherNexus.LOGGER.warn("Don't register [behavior] hook [{}] on [projectile-cypher] [{}], instead implement them on related cypher-entity directly.", module.hook, this) }

                return@filter c1 && !c2
            }.also { _implementHooksBacking = it }
        }

    open val isInvokable: Boolean = true

    fun isEmpty() = this is EmptyCypher
    fun isNotEmpty() = !isEmpty()
    /** use for AddTrigger series */
    // here only 4 situations:
    // ProjectileCypher with #triggerInterplay == true (default),   this allows trigger to attach and seen as a payload,
    //                                                              once a decent attachment is found, cyphers within the add-trigger and the attachment will be discarded
    // ProjectileCypher with #triggerInterplay == false,            add trigger-s will ignore the cypher, for example, the Notes
    // NonProjectileCypher with #triggerInterplay == true,          will terminate the searching process & cancel the discarding after, for example, blood-magic
    // NonProjectileCypher with #triggerInterplay == false (default), simply modify the state-chunk
    open fun triggerInterplay() = false


    // attr & data attach ==============================================================================================

//    fun holder(): Holder.Reference<AbstractCypher> = Cyphers.REGISTRY.getHolder(resource).getOrElse()
//    { throw CypherNotFoundException("$resource not exist") }

    fun holder(): Holder<AbstractCypher> = Cyphers.REGISTRY.wrapAsHolder(this)

    private fun getDataMap() = holder().getData(CYPHER_DATA_ATTACH)

    open fun defaultAttributes(): Builder = CypherDataMap.builder().defaultAttribute()

    fun dataMap() = getDataMap() ?: run {
        CypherNexus.LOGGER.warn("cypher $this missing attributes data, this may cause lag")
        defaultAttributes().build()
    }

    // ============================================================================================================
    /**
     * invoke a cypher when it is in hand, should be paired with [InvokingHelper.draw] or [InvokingHelper.drawNext]
     * */
    fun invokeInHand(
        helper: InvokingHelper,
        shotState: ShotStateChunk,
        data: HelperDataBundle,
        paras: InvokingParameterBundle,
    ) {
        paras.recursionDepth = 0

        traceInvoke(helper, shotState, data, paras, helper.lastDrawIndex, false)

        if (helper.invoker is ServerPlayer) {
            // TODO award stats
        }
    }

    /**
     * wrap [invoke] with an `InvokingTracer` handler.
     * */
    fun traceInvoke(
        helper: InvokingHelper,
        shotState: ShotStateChunk,
        data: HelperDataBundle,
        paras: InvokingParameterBundle,
        relativeIndex: Int,
        isCopy: Boolean,
    ) {
        helper.tracer.enter(this, shotState, data, paras, relativeIndex, isCopy)
        try {
            invoke(helper, shotState, data, paras, relativeIndex, isCopy)
        } finally {
            helper.tracer.exit(this, shotState, data, paras)
        }
    }

    /**
     * invoke the cypher.
     *
     * NOTE should avoid use this method directly,
     * use [invokeInHand] if cypher comes from a `draw`, or [IRecursiveCypher.copyCypher] if it's invoked by another cypher.
     * @param relativeIndex where the cypher should function at.
     * if the cypher comes from a draw, this should be the index of the cypher in [com.github.nahnullscience.cypher_nexus.utility.mod.ArrayOfCyphers]
     * */
    protected open fun invoke(
        helper: InvokingHelper,
        shotState: ShotStateChunk,
        data: HelperDataBundle,
        paras: InvokingParameterBundle,
        relativeIndex: Int,
        isCopy: Boolean,
    ) {
        CypherNexus.debugCypher { "[$this $relativeIndex] is invoked and modifies the state" }
        modifyShotState(helper, shotState, data, paras, isCopy)
        handleDraws(helper, shotState, data, paras)
    }


    /**
     * if draw-enabled, draw [draw] times, then invoke drawn cyphers one by one
     * */
    protected fun handleDraws(
        helper: InvokingHelper,
        shotState: ShotStateChunk,
        data: HelperDataBundle,
        paras: InvokingParameterBundle,
    ) {
        if (paras.drawEnabled && draw > 0)
        drawXForEach(helper, draw) { index, cypher ->
            cypher.invokeInHand(helper, shotState, data, paras)
        }
    }

    /**
     * bare draw logic, execute [consumer] when drawn non-null. should prefer [handleDraws] in most case
     * */
    protected inline fun drawXForEach(
        helper: InvokingHelper,
        X: Int,
        consumer: (index: Int, cypher: AbstractCypher) -> Unit
    ) {
        for (i in 0 until X) {
            var cy = helper.drawNext()
            if (cy == null) {
                CypherNexus.debugCypher { "[$this] want a wrap" }
                val wrap = helper.wrap()
                if (!wrap) break // nothing to wrap, break
                cy = helper.drawNext()
            }

            if (cy != null) {
                consumer(helper.lastDrawIndex, cy)
            }
        }
    }

    /**
     * record this cypher on `ccMap`
     * */
    open fun modifyShotState(
        helper: InvokingHelper,
        shotState: ShotStateChunk,
        data: HelperDataBundle,
        paras: InvokingParameterBundle,
        isCopy: Boolean
    ) {
        shotState.record(this)

        if (!isCopy) { // a copy should not affect mana / delay / recharge
            if (shotState.isRoot) data.delay += delay
            data.recharge += recharge
        }

        helper.tracer.modify(helper, shotState, data, paras, isCopy)
    }

    // ============================================================================================================

    override fun toString(): String = resource.path

    // since cyphers are in the same registry, their names are unlikely to repeat,
    // but using category prefix can make it tidy
    /**
     * lang-JSON key: cypher.{MOD_ID}.{cypher_category}.{cypher_name}
     * */
    override fun translation(): MutableComponent = Component.translatable(
            _translationKeyBacking ?:
            "cypher.${resource.namespace}.${category.value().registryName()}.${resource.path}".also { _translationKeyBacking = it }
    )
    private var _translationKeyBacking: String? = null

    /**
     * lang-JSON key: cypher.{MOD_ID}.{cypher_category}.{cypher_name}.description
     * */
    open fun description(): MutableComponent = Component.translatable(
        _descriptionKeyBacking ?:
        "cypher.${resource.namespace}.${category.value().registryName()}.${resource.path}.description".also { _descriptionKeyBacking = it }
    )
    private var _descriptionKeyBacking: String? = null

    /**
     * icons: {MOD_ID}/textures/cypher/{cypher_category}/{cypher_name}.png
     * */
    open val texture: Identifier get() {
        return _textureBacking ?: Identifier.fromNamespaceAndPath(
            resource.namespace,
            "textures/cypher/${category.value().registryName()}/${resource.path}.png"
        ).also { _textureBacking = it }
    }
    private var _textureBacking: Identifier? = null

    /**
     * detailed tooltip in index-screen
     * */
    open val attributesTooltip: List<Component> get() {
        return _attrTooltipBacking ?: run {
            // since attributes won't change once initialized
            val components = mutableListOf<Component>()

            components.add(CategoryRow.row(category.value()))
            if (pattern != NO_PATTERN) components.add(PatternRow.row(pattern.value()))
            if (manaDrain != 0f) components.add(ManaDrainRow.row(manaDrain))
            if (draw > 1) components.add(DrawRow.row(draw))
            if (delay != 0) components.add(CastDelayRow.row(delay))
            if (recharge != 0) components.add(RechargeTimeRow.row(recharge))

            // keep the order attrs registered
            CypherAttributes.REGISTRY.forEach registry@ { attribute ->
                if (attribute.hide) return@registry
                val opMap = dataMap().shotState.getOrElse(attribute) { return@registry }

                var values: MutableComponent? = null
                AttributeOperator.entries.forEach enum@ { operator ->
                    var v = opMap.getOrElse(operator) { return@enum }
                    val c: MutableComponent
                    if (operator.needUnit) {
                        v = attribute.parseUnit(v)
                        val t = operator.format(v, attribute.formatter ?: operator.defaultFormatter)
                        c = attribute.wrapWithUnit(t)
                    } else {
                        val t = operator.format(v)
                        c = Component.literal(t)
                    }

                    if (values == null) values = c
                    else values.append(";  ").append(c)
                }

                values?.let {
                    components.add(attribute.displayRow(it.withStyle(ChatFormatting.GOLD)))
                }
            }

            return@run components
        }.also { _attrTooltipBacking = it }
    }
    private var _attrTooltipBacking: List<Component>? = null
}