package com.github.nahnullscience.cypher_nexus.init.mod

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookModule
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.invoking.InvokeRedirectPosHook
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.BeforeDiscardHook
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.FirstTickHook
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.HitEntityHook
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.TickBehaviorHook
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.RegistryBuilder
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS
import java.util.function.Supplier
import kotlin.reflect.KClass

object CypherBehaviorHookRegistry {
    val RESOURCE_KEY: ResourceKey<Registry<HookModule<*>>> =
        ResourceKey.createRegistryKey(CypherNexus.modResource("cypher/hook"))
    val REGISTRY: Registry<HookModule<*>> = RegistryBuilder(RESOURCE_KEY).sync(true).create()

    val DEFERRED_REGISTER: DeferredRegister<HookModule<*>> =
        DeferredRegister.create(REGISTRY, CypherNexus.MOD_ID)

    fun register() {
        DEFERRED_REGISTER.register(MOD_BUS)
    }

    fun <T : Any> registerHook(path: String, hook: KClass<T>, target: HookModule.HookType): Supplier<out HookModule<T>> {
        // I find checking DeferredHolder<R, T>'s type is annoying...
        return DEFERRED_REGISTER.register(path) { resource -> HookModule(resource, hook, type = target) }
    }

    val INVOKE_REDIRECT_POS = registerHook("invoke_redirect_pos", InvokeRedirectPosHook::class, HookModule.HookType.INVOKING)

    val BEFORE_DISCARD = registerHook("before_discard", BeforeDiscardHook::class, HookModule.HookType.PROJECTILE)
    val FIRST_TICK = registerHook("first_tick", FirstTickHook::class, HookModule.HookType.PROJECTILE)
    val HIT_ENTITY = registerHook("hit_entity", HitEntityHook::class, HookModule.HookType.PROJECTILE)
    val TICK_BEHAVIOR = registerHook("tick_behavior", TickBehaviorHook::class, HookModule.HookType.PROJECTILE)
}