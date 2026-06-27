package com.github.nahnullscience.cypher_nexus.mechanic.cypher

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.ModDataMaps.CYPHER_DATA_ATTACH
import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.init.mod.CypherBehaviorHooks
import com.github.nahnullscience.cypher_nexus.init.mod.Cyphers
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.AttributeOperator
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.category.CypherCategory
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookModule
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.HelperDataBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.InvokingStateBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileStateChunk
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
sealed class AbstractCypher: IRegisterable {
    abstract val category: Holder<CypherCategory>

    val manaDrain: Float get() = attributes().manaDrain
    val draw: Int get() = attributes().draw
    val delay: Int get() = attributes().delay
    val recharge: Int get() = attributes().recharge
    val flags: Int get() = attributes().flags

    /** whether the cypher shows in the index(left side) */
    open val hide: Boolean = false
    /** override colors from category */
    open val color: Int = 0

    open val isInvokable: Boolean = true

    /** auto-detect hooks */
    val implementedHooks: List<HookModule<*>> by lazy {
        if (this is AbstractProjectileCypher) {
            CypherNexus.LOGGER.warn("Don't register hooks on [{}], instead implement them on related entity directly.", this)
            return@lazy emptyList()
        }
        val hookModules = CypherBehaviorHooks.REGISTRY
        hookModules.filter { it.hook.isInstance(this) }
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

    open fun defaultAttributes(): CypherDataMap.Builder = CypherDataMap.builder()

    fun attributes() = attributesData() ?: run { CypherNexus.LOGGER.warn("cypher $this missing attributes data, this may cause lag"); defaultAttributes().build() }

    // ============================================================================================================
    /** when invoke from helper#draw */
    fun invokeInHand(
        helper: InvokingHelper,
        chunk: ProjectileStateChunk,
        data: HelperDataBundle,
        state: InvokingStateBundle,
    ) {
        state.recursionDepth = 0
        invoke(helper, chunk, data, state, helper.lastDrawIndex, false)

        if (helper.invoker is ServerPlayer) {
            // TODO award stats
        }
    }

    /** call super# for basic behaviors
     * @param relativeIndex where the cypher should function at.
     * if the cypher comes from a draw, this should be the index of the cypher in [com.github.nahnullscience.cypher_nexus.utility.mod.ArrayOfCyphers]
     * */
    open fun invoke(
        helper: InvokingHelper,
        chunk: ProjectileStateChunk,
        data: HelperDataBundle,
        state: InvokingStateBundle,
        relativeIndex: Int,
        isCopy: Boolean,
    ) {
        CypherNexus.debugCypher { "[$this $relativeIndex] is invoked and modifies the state" }
        modifyStateChunk(helper, data, chunk)

        var forwardState = chunk
        if (this is AbstractProjectileCypher) {
            forwardState = addToStateChunk(chunk)
        }

        handleDraws(helper, forwardState, data, state)
    }


    /**
     * if drawEnabled, draw [draw] times, then invoke drawn cyphers one by one
     * */
    protected fun handleDraws(
        helper: InvokingHelper,
        chunk: ProjectileStateChunk,
        data: HelperDataBundle,
        state: InvokingStateBundle,
    ) {
        if (state.drawEnabled)
        drawXForEach(helper, draw) { index, cypher ->
            cypher.invokeInHand(helper, chunk, data, state)
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

    open fun modifyStateChunk(helper: InvokingHelper, data: HelperDataBundle, chunk: ProjectileStateChunk) {

        chunk.record(this)

        if (chunk.isRoot) data.delay += delay
        data.recharge += recharge
    }

    // ============================================================================================================

    override fun toString(): String = resource.path

    // since cyphers are in the same registry, their names are unlikely to repeat,
    // but using category prefix can make it tidy
    /**
     * lang-JSON key: cypher.{MOD_ID}.{cypher_category}.{cypher_name}
     * */
    private fun translationKey() = "cypher.${resource.namespace}.${category.value().registryName()}.${resource.path}"
    override fun translation(): MutableComponent = Component.translatable(translationKey())
    open fun description(): MutableComponent = Component.translatable("${translationKey()}.description")

    /** icons: {MOD_ID}/textures/cypher/{cypher_category}/{cypher_name}.png */
    open fun texture(): Identifier =
        Identifier.fromNamespaceAndPath(resource.namespace, "textures/cypher/${category.value().registryName()}/${resource.path}.png")

    /** detailed tooltip in index-screen */
    open val attributesTooltip: List<MutableComponent> by lazy {
        // since attributes won't change once initialized
        val components = mutableListOf<MutableComponent>()

        val cate = Component.literal("  ")
            .append(Component.translatable("cypher.attribute.${CypherNexus.MOD_ID}.category"))
            .append(Component.literal(": "))
            .append(category.value().translation().withStyle(ChatFormatting.YELLOW))
        components.add(cate)

        val mana = Component.literal("  ")
            .append(Component.translatable("cypher.attribute.${CypherNexus.MOD_ID}.mana_drain")) // not attribute though keeping lang format
            .append(Component.literal(": "))
            .append(Component.literal("$manaDrain").withStyle(ChatFormatting.YELLOW))
        components.add(mana)

        if (draw > 1) {
            val compon = Component.literal("  ")
                .append(Component.translatable("cypher.attribute.${CypherNexus.MOD_ID}.draw"))
                .append(Component.literal(": "))
                .append(Component.literal("$draw").withStyle(ChatFormatting.YELLOW))
            components.add(compon)
        }

        if (delay != 0) {
            val compon = Component.literal("  ")
                .append(Component.translatable("cypher.attribute.${CypherNexus.MOD_ID}.delay"))
                .append(Component.literal(": "))
                .append(Component.literal("$delay").withStyle(ChatFormatting.YELLOW))
            components.add(compon)
        }

        if (recharge != 0) {
            val compon = Component.literal("  ")
                .append(Component.translatable("cypher.attribute.${CypherNexus.MOD_ID}.recharge"))
                .append(Component.literal(": "))
                .append(Component.literal("$recharge").withStyle(ChatFormatting.YELLOW))
            components.add(compon)
        }

        // keep the order attrs registered
        CypherAttributes.REGISTRY.forEach registry@ { attribute ->
            if (attribute.hide) return@registry
            val opMap = attributes().stateChunk.getOrElse(attribute) { return@registry }
            var values: MutableComponent? = null
            AttributeOperator.entries.forEach enum@ { op ->
                val v = opMap.getOrElse(op) { return@enum }
                if (values == null) values = op.format(v)
                else values.append("; ").append(op.format(v))
            }
            val comp = Component.literal("  ")
                .append(attribute.translation())
                .append(Component.literal(": "))
                .append(values ?: Component.literal("ERROR").withStyle(ChatFormatting.YELLOW))
            components.add(comp)
        }

        components
    }
}