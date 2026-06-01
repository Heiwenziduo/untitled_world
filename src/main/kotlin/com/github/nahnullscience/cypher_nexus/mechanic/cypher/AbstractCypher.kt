package com.github.nahnullscience.cypher_nexus.mechanic.cypher

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.ModDataAttachments.CYPHER_DATA_ATTACH
import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.init.mod.CypherBehaviorHooks
import com.github.nahnullscience.cypher_nexus.init.mod.Cyphers
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttributeOperation
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.category.CypherCategory
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookModule
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileStateChunk
import com.github.nahnullscience.cypher_nexus.utility.exception.CypherNotFoundException
import com.github.nahnullscience.cypher_nexus.utility.i.IRegisterable
import net.minecraft.ChatFormatting
import net.minecraft.core.Holder
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation
import kotlin.jvm.optionals.getOrElse

/**
 *
 * */
sealed class AbstractCypher: IRegisterable {
    val manaDrain: Float
        get() = attributes().manaDrain
    val draw: Int
        get() = attributes().draw
    val delay: Int
        get() = attributes().delay
    val recharge: Int
        get() = attributes().recharge

    /** whether the cypher shows in the index(left side) */
    open val hide: Boolean = false
    /** override colors from category */
    open val color: Int = 0
    private var _flag: Int = 0
    /** use #addFlag during init */
    val flag: Int
        get() = _flag

    /** auto detect hooks */
    val implementedHooks: List<HookModule<*>> by lazy { // lazy init and cache result, cool
        val hookModules = CypherBehaviorHooks.REGISTRY
        hookModules.filter { it.hook.isInstance(this) }
    }

    open val isRecursive: Boolean = false
    fun isEmpty() = this is EmptyCypher
    fun isNotEmpty() = !isEmpty()
    /** use for AddTrigger series */
    open fun triggerCanAttach() = false
    /** use for AddTrigger series */
    open fun triggerCanPayload() = false
    open fun isInvokable() = true


    abstract val category: Holder<CypherCategory>


    // = attr & data attach ==============================================================================================

//    fun holder(): Holder.Reference<AbstractCypher> = Cyphers.REGISTRY.getHolder(resource).getOrElse()
//    { throw CypherNotFoundException("$resource not exist") }

    fun holder(): Holder<AbstractCypher> = Cyphers.REGISTRY.wrapAsHolder(this)

    private fun attributesData() = holder().getData(CYPHER_DATA_ATTACH)

    open fun defaultAttributes(): CypherDataAttach.Builder = CypherDataAttach.builder()

    fun attributes() = attributesData() ?: run { CypherNexus.LOGGER.warn("cypher $this missing attributes data, this may cause lag"); defaultAttributes().build() }

    // ============================================================================================================
    /** call super# unless you know what you are doing */
    open fun invokeInHand(
        helper: InvokingHelper,
        chunk: ProjectileStateChunk,
        data: InvokingHelper.HelperDataBundle,
        state: InvokingHelper.HelperStateBundle,
        options: CypherInvokingOptions = CypherInvokingOptions()
    ) {
        CypherNexus.LOGGER.debug("[{}] is invoked", this)
        modifyStateChunk(helper, chunk)
        var forwardState = chunk
        if (this is AbstractProjectileCypher) {
            forwardState = addToStateChunk(helper, chunk)
        }

        if (options.drawEnabled) handleDraws(helper, forwardState, data, state, options)
    }
    protected fun handleDraws(
        helper: InvokingHelper,
        chunk: ProjectileStateChunk,
        data: InvokingHelper.HelperDataBundle,
        state: InvokingHelper.HelperStateBundle,
        options: CypherInvokingOptions = CypherInvokingOptions()
    ) {
        for (i in 0 until draw) {
            var cy = helper.drawNext()
            if (cy == null) {
                CypherNexus.LOGGER.debug("[{}] want a wrap", this)
                val wrap = helper.wrap()
                if (!wrap) break // nothing to wrap, break
                cy = helper.drawNext()
            }
            cy?.invokeInHand(helper, chunk, data, state)
        }
    }

    open fun modifyStateChunk(helper: InvokingHelper, chunk: ProjectileStateChunk) {
        attributes().stateChunk.forEach { attribute, cyMap ->
            // prune 1, sub-chunk do not affect delay, spread, whatsoever. note recharge is an exception
            if (chunk != helper.rootChunk && attribute.applyOn == CypherAttribute.AttributeApply.INVOKING) return@forEach

            val stateMap = chunk.computedOperationMap.getOrPut(attribute) { HashMap() }
            // prune 2, if set, skip
            if (stateMap[CypherAttributeOperation.SET_ALL] != null && cyMap[CypherAttributeOperation.SET_ALL] == null) return@forEach

            cyMap.forEach { operator, value ->
                if (operator != CypherAttributeOperation.BASE && operator != CypherAttributeOperation.SET_SELF) {
                    stateMap.compute(operator) { op, v -> operator.cumulate(v?: operator.defaultValue, value) }
                }
            }
        }
        if (this is AbstractNonProjectileCypher) {
            CypherNexus.LOGGER.debug("[{}] modifies the state", this)
            // hooks on NonProjectile affect the Block, hooks on Projectile only affect itself
            chunk.attachHooks(this)
            chunk.enableFlags(flag)
        }
    }

//    open fun discardFromDeck(helper: InvokingHelper,) {
//
//    }

    // ============================================================================================================

    override fun toString(): String = resource.path

    // since cyphers are in the same registry, their names are unlikely to repeat,
    // but using category prefix can make it tidy
    private fun translationKey(): String = "cypher.${resource.namespace}.${category.value().registryName()}.${resource.path}"

    /**
     * lang-JSON key: cypher.{MOD_ID}.{cypher_category}.{cypher_name}?.{key}
     * @param key represents name if empty
     * */
    open fun translation(key: TranslationKey?): MutableComponent =
        Component.translatable("${translationKey()}${if (key==null) "" else ".$key"}")
    override fun translation(): MutableComponent = translation(null)

    /** icons: {MOD_ID}/textures/cypher/{cypher_category}/{cypher_name}.png */
    open fun texture(): ResourceLocation =
        ResourceLocation.fromNamespaceAndPath("${resource.namespace}",
            "textures/cypher/${category.value().registryName()}/${resource.path}.png")

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
            val draw = Component.literal("  ")
                .append(Component.translatable("cypher.attribute.${CypherNexus.MOD_ID}.draw"))
                .append(Component.literal(": "))
                .append(Component.literal("$draw").withStyle(ChatFormatting.YELLOW))
            components.add(draw)
        }

        // keep the order attrs registered
        CypherAttributes.REGISTRY.forEach registry@ { attribute ->
            if (attribute.hide) return@registry
            val opMap = attributes().stateChunk.getOrElse(attribute) { return@registry }
            var values: MutableComponent? = null
            CypherAttributeOperation.entries.forEach op@ { op ->
                val v = opMap.getOrElse(op) { return@op }
                if (values == null) values = op.format(v)
                else values.append("; ").append(op.format(v))
            }
            val comp = Component.literal("  ")
                .append(attribute.translation())
                .append(Component.literal(": "))
                .append(values?: Component.literal("ERROR").withStyle(ChatFormatting.YELLOW))
            components.add(comp)
        }

        components
    }



    enum class TranslationKey() {
        DESCRIPTION, // -> .description
        ;

        override fun toString(): String {
            return this.name.lowercase()
        }
    }

    data class CypherInvokingOptions(
        val drawEnabled: Boolean = true,
        val recursiveDepth: Int = 0,
    ) {

    }
}