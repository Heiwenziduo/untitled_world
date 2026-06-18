package com.github.nahnullscience.cypher_nexus.mechanic.cypher

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.ModDataMaps.CYPHER_DATA_ATTACH
import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes.RECOIL
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
import java.util.EnumMap

/**
 *
 * */
sealed class AbstractCypher: IRegisterable {
    companion object {
        const val RECURSION_LIMIT = 2
    }

    val manaDrain: Float get() = attributes().manaDrain
    val draw: Int get() = attributes().draw
    val delay: Int get() = attributes().delay
    val recharge: Int get() = attributes().recharge
    val flags: Int get() = attributes().flags

    /** whether the cypher shows in the index(left side) */
    open val hide: Boolean = false
    /** override colors from category */
    open val color: Int = 0

    /** auto-detect hooks */
    val implementedHooks: List<HookModule<*>> by lazy {
        if (this is AbstractProjectileCypher) {
            CypherNexus.LOGGER.warn("Don't register hooks on [{}], instead implement them on related entity directly.", this)
            return@lazy emptyList()
        }
        val hookModules = CypherBehaviorHooks.REGISTRY
        hookModules.filter { it.hook.isInstance(this) }
    }
    /** if a cypher may call itself, set this to true to avoid infinite loop */
    open val isRecursive: Boolean = false
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
    open fun isInvokable() = true


    abstract val category: Holder<CypherCategory>


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
        invoke(helper, chunk, data, state, helper.relativeIndex, 0, false)

        if (helper.invoker is ServerPlayer) {
            // TODO award stats
        }
    }

    /** call super# for basic behaviors
     * @param relativeIndex represent current invoking cypher's Index in the AoC */
    // build the depth-first tree
    open fun invoke(
        helper: InvokingHelper,
        chunk: ProjectileStateChunk,
        data: HelperDataBundle,
        state: InvokingStateBundle,
        relativeIndex: Int,
        recursionDepth: Int,
        isCopy: Boolean,
    ) {
        if (!canRecursionContinue(recursionDepth)) return
        CypherNexus.debugCypher { "[$this $relativeIndex] is invoked and modifies the state" }
        modifyStateChunk(helper, data, chunk)

        var forwardState = chunk
        if (this is AbstractProjectileCypher) {
            forwardState = addToStateChunk(chunk)
        }

        handleDraws(helper, forwardState, data, state)
    }

    protected fun canRecursionContinue(recursionDepth: Int,): Boolean {
        if (isRecursive) {
            if (recursionDepth > RECURSION_LIMIT) {
                CypherNexus.debugCypher { "[$this] is stopped due to recursion limit" }
                return false
            }
        }
        return true
    }

//    protected open fun canDraw(
//        helper: InvokingHelper,
//        chunk: ProjectileStateChunk,
//        data: HelperDataBundle,
//        state: InvokingStateBundle,
//    ): Boolean {
//        return state.drawEnabled
//    }

    protected fun handleDraws(
        helper: InvokingHelper,
        chunk: ProjectileStateChunk,
        data: HelperDataBundle,
        state: InvokingStateBundle,
    ) {
        if (state.drawEnabled)
        for (i in 0 until draw) {
            var cy = helper.drawNext()
            if (cy == null) {
                CypherNexus.debugCypher { "[$this] want a wrap" }
                val wrap = helper.wrap()
                if (!wrap) break // nothing to wrap, break
                cy = helper.drawNext()
            }
            cy?.invokeInHand(helper, chunk, data, state)
        }
    }

    open fun modifyStateChunk(helper: InvokingHelper, data: HelperDataBundle, chunk: ProjectileStateChunk) {
        if (chunk == helper.rootChunk) data.delay += delay
        data.recharge += recharge

        attributes().stateChunk.forEach { (attribute, cyMap) ->
            var targetChunk = chunk
            if (RECOIL.`is`(attribute.resource)) targetChunk = helper.rootChunk

            val chunkMap = targetChunk.computedOperationMap.getOrPut(attribute) { EnumMap(AttributeOperator::class.java) }
            // prune: if set, skip
            if (chunkMap[AttributeOperator.SET_ALL] != null && cyMap[AttributeOperator.SET_ALL] == null) return@forEach

            cyMap.forEach { (operator, value) ->
                if (operator != AttributeOperator.BASE) {
                    chunkMap.compute(operator) { op, v -> operator.cumulate(v?: operator.defaultValue, value) }
                }
            }
        }
        if (this is AbstractNonProjectileCypher) {
            // hooks on NonProjectile affect the Block, hooks on Projectile only affect itself
            chunk.attachHooks(this)
            chunk.enableFlags(flags)
        }

        chunk.record(this)
    }

//    open fun discardFromDeck(helper: InvokingHelper,) {
//
//    }

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