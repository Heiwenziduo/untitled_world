package com.github.nahnullscience.cypher_nexus.mechanic.cypher

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.data_driven.ModDataMaps.CYPHER_DATA_ATTACH
import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.init.mod.CypherHooks
import com.github.nahnullscience.cypher_nexus.init.mod.Cyphers
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap.Builder
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherProperties.*
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.AttributeOperator
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.category.CypherCategory
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookModule
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookModule.HookType
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

    val manaDrain: Float get() = attributes().manaDrain
    val draw: Int get() = attributes().draw
    val delay: Int get() = attributes().delay
    val recharge: Int get() = attributes().recharge
    val flags: Int get() = attributes().flags

    /** whether the cypher shows in the index(left side) */
    open val hide: Boolean = false
    /** override colors from category */
    open val color: Int? = null

    open val isInvokable: Boolean = true

    /** auto-detect hooks */
    val implementedHooks: List<HookModule<*>> by lazy {
        CypherHooks.REGISTRY.filter { module ->
            val c1 = module.hook.isInstance(this)
            val c2 = (this is AbstractProjectileCypher<*> && module.type == HookType.BEHAVIOR)
                .also { if (it) CypherNexus.LOGGER.warn("Don't register [behavior] hook [{}] on [{}], instead implement them on related entity directly.", module.hook, this) }

            return@filter c1 && !c2
        }
    }
//    /** if a cypher may call itself, set this to true to avoid infinite loop */
//    open val isRecursive: Boolean = false
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

    private fun attributesData() = holder().getData(CYPHER_DATA_ATTACH)

    open fun defaultAttributes(): Builder = CypherDataMap.builder().defaultAttribute()

    fun attributes() = attributesData() ?: run {
        CypherNexus.LOGGER.warn("cypher $this missing attributes data, this may cause lag")
        defaultAttributes().build()
    }

    // ============================================================================================================
    /** when invoke from helper#draw */
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
        modifyShotState(helper, data, shotState)

        var nextState = shotState
        if (this is AbstractProjectileCypher<*>) {
            nextState = addToShotState(shotState)
        }

        handleDraws(helper, nextState, data, paras)
    }


    /**
     * if drawEnabled, draw [draw] times, then invoke drawn cyphers one by one
     * */
    protected fun handleDraws(
        helper: InvokingHelper,
        shotState: ShotStateChunk,
        data: HelperDataBundle,
        paras: InvokingParameterBundle,
    ) {
        if (paras.drawEnabled)
        drawXForEach(helper, draw) { index, cypher ->
            cypher.invokeInHand(helper, shotState, data, paras)
        }
    }

    /**
     * bare draw logic, should prefer [handleDraws] in most case
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

    open fun modifyShotState(helper: InvokingHelper, data: HelperDataBundle, shotState: ShotStateChunk) {

        shotState.record(this)

        if (shotState.isRoot) data.delay += delay
        data.recharge += recharge
    }

    // ============================================================================================================

    override fun toString(): String = resource.path

    // since cyphers are in the same registry, their names are unlikely to repeat,
    // but using category prefix can make it tidy
    /**
     * lang-JSON key: cypher.{MOD_ID}.{cypher_category}.{cypher_name}
     * */
    private val translationKey by lazy { "cypher.${resource.namespace}.${category.value().registryName()}.${resource.path}" }
    private val descriptionKey by lazy { "cypher.${resource.namespace}.${category.value().registryName()}.${resource.path}.description" }
    override fun translation(): MutableComponent = Component.translatable(translationKey)
    open fun description(): MutableComponent = Component.translatable(descriptionKey)

    /** icons: {MOD_ID}/textures/cypher/{cypher_category}/{cypher_name}.png */
    open val texture by lazy {
        Identifier.fromNamespaceAndPath(
            resource.namespace,
            "textures/cypher/${category.value().registryName()}/${resource.path}.png"
        )
    }

    /** detailed tooltip in index-screen */
    open val attributesTooltip: List<Component> by lazy {
        // since attributes won't change once initialized
        val components = mutableListOf<Component>()

        components.add(CategoryRow.row(category.value()))
        if (manaDrain != 0f) components.add(ManaDrainRow.row(manaDrain))
        if (draw > 1) components.add(DrawRow.row(draw))
        if (delay != 0) components.add(CastDelayRow.row(delay))
        if (recharge != 0) components.add(RechargeTimeRow.row(recharge))

        // keep the order attrs registered
        CypherAttributes.REGISTRY.forEach registry@ { attribute ->
            if (attribute.hide) return@registry
            val opMap = attributes().shotState.getOrElse(attribute) { return@registry }

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

        return@lazy components
    }
}