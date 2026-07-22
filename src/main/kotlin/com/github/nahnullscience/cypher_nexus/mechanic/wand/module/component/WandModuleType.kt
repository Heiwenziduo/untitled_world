package com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component

import com.github.nahnullscience.cypher_nexus.utility.i.IRegisterable
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.Identifier
import kotlin.reflect.KClass

class WandModuleType <out Module : IWandModule> (
    override val resource: Identifier,
//    val module: KClass<out Module>,
): IRegisterable {

    override fun toString(): String {
        return "[module type: ${resource.path}]"
    }

    private val translationKey by lazy { "wand.module.${resource.namespace}.${resource.path}" }

    /** lang-JSON key: wand.module.{MOD_ID}.{module_name} */
    override fun translation(): MutableComponent = Component.translatable(translationKey)
}