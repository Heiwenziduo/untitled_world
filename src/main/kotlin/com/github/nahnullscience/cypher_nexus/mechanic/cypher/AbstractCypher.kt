package com.github.nahnullscience.cypher_nexus.mechanic.cypher

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.init.mod.CypherBehaviorHookRegistry
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttributeOperation
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.category.CypherCategory
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookModule
import com.github.nahnullscience.cypher_nexus.utility.i.IRegisterable
import net.minecraft.ChatFormatting
import net.minecraft.core.Holder
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

/**
 *
 * */
sealed class AbstractCypher: IRegisterable {
    open val manaDrain: Float = 0f
    open val draw: Int = 0
    /** whether the cypher shows in the index(left side) */
    open val hide: Boolean = false
    /** override colors from category */
    open val color: Int = 0
    private var _flag: Int = 0
    /** use #addFlag during init */
    val flag: Int
        get() = _flag
    private val _attributeMap = HashMap<Holder<CypherAttribute>, HashMap<CypherAttributeOperation, Double>>()
    val attributeMap
        get() = _attributeMap
    /** auto detect hooks */
    val implementHooks: List<HookModule<*>> by lazy { // lazy init and cache result, cool
        val hookModules = CypherBehaviorHookRegistry.REGISTRY
        hookModules.filter { it.hook.isInstance(this) }
    }

    private var _initLock = false // this seems unnecessary

    abstract val category: Holder<CypherCategory>
    abstract override val resource: ResourceLocation

    init {
        initializeData()
    }

    /**
     * add Attributes with its BASE value
     * */
    protected open fun addAttribute(holder: Holder<CypherAttribute>, base: Double): AbstractCypher {
        return addAttribute(holder, CypherAttributeOperation.BASE, base)
    }
    /**
     * add Attributes with specific operator
     * */
    protected fun addAttribute(holder: Holder<CypherAttribute>, operator: CypherAttributeOperation, value: Double?): AbstractCypher {
        if (!_initLock) {
            val map = _attributeMap.getOrPut(holder) { HashMap() }
            if (value != null) map.compute(operator) { k,v -> operator.cumulate(v?: operator.defaultValue, value) }
        }
        else CypherNexus.LOGGER.fatal("try add attribute ${holder.registeredName} while map is locked!")
        return this
    }
    /**
     * add Attributes to the helper
     * */
    fun addAttribute(helper: CypherInvokerHelper) {
        helper.addAttribute(_attributeMap)
    }


    protected open fun initializeData() {
        // TODO: read from json
    }

    /**
     * register a hook to modifier projectile-AI at specific moments
     * */
//    protected fun registerHooks(hooks: Supplier<out HookModule<*>>) {
//        if (!_initLock) _hookList.add(hooks)
//    }

    protected fun addFlag(flags: CypherFlags): AbstractCypher {
        _flag = _flag or flags.value
        return this
    }


    /** custom logic up to subclasses */
    // TODO maybe change this to "hook"
    open fun onInvokeServer(level: Level, caster: Entity?, stack: ItemStack?, helper: CypherInvokerHelper, wandLength: Float) {}

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
        for (holder in CypherAttributes.REGISTRY.holders()) {
            if (holder.value().hide) continue
            val opMap = _attributeMap.getOrElse(holder) { continue }
            var values: MutableComponent? = null
            CypherAttributeOperation.entries.forEach { op ->
                val v = opMap.getOrElse(op) { return@forEach }
                if (values == null) values = op.format(v)
                else values.append("; ").append(op.format(v))
            }
            val comp = Component.literal("  ")
                .append(holder.value().translation())
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
}