package com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook

import com.github.nahnullscience.cypher_nexus.utility.i.IRegisterable
import net.minecraft.resources.ResourceLocation
import kotlin.reflect.KClass

class HookModule <out HOOK : Any> (
    override val resource: ResourceLocation,
    val hook: KClass<out HOOK>,
    val sync: Boolean,
    val type: HookType,
    val unique: Boolean = false,
): IRegisterable {

    override fun toString(): String {
        return "${hook.simpleName}"
    }

    enum class HookType {
        INVOKING, // FIXME necessary? since invoking process always on server
        PROJECTILE
    }
}