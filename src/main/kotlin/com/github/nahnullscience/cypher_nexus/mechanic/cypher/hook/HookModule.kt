package com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook

import com.github.nahnullscience.cypher_nexus.utility.i.IRegisterable
import net.minecraft.resources.Identifier
import kotlin.reflect.KClass

class HookModule <out Hook : Any> (
    override val resource: Identifier,
    val hook: KClass<out Hook>,
    val sync: Boolean,
    val type: HookType,
    val unique: Boolean = false,
): IRegisterable {

    override fun toString(): String {
        return "${hook.simpleName}"
    }

    enum class HookType {
        INVOKING,
        PROJECTILE
    }
}