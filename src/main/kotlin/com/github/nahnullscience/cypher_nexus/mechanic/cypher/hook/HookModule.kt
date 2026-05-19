package com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook

import com.github.nahnullscience.cypher_nexus.utility.i.IRegisterable
import net.minecraft.resources.ResourceLocation
import kotlin.reflect.KClass

class HookModule <HOOK : Any> (
    override val resource: ResourceLocation,
    val hook: KClass<HOOK>,
    val sync: Boolean = true,
    val type: HookType
): IRegisterable {

    override fun toString(): String {
        return "module_${hook.simpleName}"
    }

    enum class HookType {
        INVOKING,
        PROJECTILE
    }
}