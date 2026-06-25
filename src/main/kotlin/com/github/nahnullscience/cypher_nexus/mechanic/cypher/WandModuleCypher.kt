package com.github.nahnullscience.cypher_nexus.mechanic.cypher

import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategories
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.ItemWandInstance
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.MapOfModules

abstract class WandModuleCypher : AbstractNonProjectileCypher() {
    final override val category = CypherCategories.WAND_MODULE
    final override fun isInvokable() = false
    final override fun triggerInterplay() = false

    abstract fun apply(instance: ItemWandInstance, modules: MapOfModules)

//    override val attributesTooltip: List<MutableComponent> by lazy {
//        val components = mutableListOf<MutableComponent>()
//        val cate = Component.literal("  ")
//            .append(Component.translatable("cypher.attribute.${CypherNexus.MOD_ID}.category"))
//            .append(Component.literal(": "))
//            .append(category.value().translation().withStyle(ChatFormatting.YELLOW))
//        components.add(cate)
//    }

}