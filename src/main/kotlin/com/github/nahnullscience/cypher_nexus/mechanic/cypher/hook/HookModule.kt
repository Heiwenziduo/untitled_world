package com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.utility.i.IRegisterable
import net.minecraft.resources.Identifier
import kotlin.reflect.KClass

class HookModule <out Hook : Any> (
    override val resource: Identifier,
    val hook: KClass<out Hook>,
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

    class HookBuilder <out Hook : Any> (path: String, val hook: KClass<out Hook>) {
        val resource = CypherNexus.modResource(path)
        private var type = HookType.PROJECTILE
        private var uniqueness: Boolean = false

        fun invoking() = apply { type = HookType.INVOKING }
        fun unique() = apply { uniqueness = true }
        fun build() = HookModule<Hook>(resource, hook, type, uniqueness)
    }
}