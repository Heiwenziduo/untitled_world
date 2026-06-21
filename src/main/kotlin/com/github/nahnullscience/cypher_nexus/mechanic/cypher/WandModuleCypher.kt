package com.github.nahnullscience.cypher_nexus.mechanic.cypher

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategories
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.IWandModule
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.ModuleCategory
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import java.util.EnumMap

abstract class WandModuleCypher : AbstractNonProjectileCypher() {
    final override val category = CypherCategories.WAND_MODULE
    final override fun isInvokable() = false
    final override fun triggerInterplay() = false

    abstract fun apply(modules: EnumMap<ModuleCategory, IWandModule>)
//    override val attributesTooltip: List<MutableComponent> by lazy {
//        val components = mutableListOf<MutableComponent>()
//        val cate = Component.literal("  ")
//            .append(Component.translatable("cypher.attribute.${CypherNexus.MOD_ID}.category"))
//            .append(Component.literal(": "))
//            .append(category.value().translation().withStyle(ChatFormatting.YELLOW))
//        components.add(cate)
//    }
}