package com.github.nahnullscience.cypher_nexus.init.mod

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookModule
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookModule.HookType
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.invoking.ServerInvokeRedirectPosHook
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.BothBeforeDiscardHook
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.BothFirstTickHook
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.ServerHitEntityHook
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.BothTickBehaviorHook
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.RegistryBuilder
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS
import java.util.function.Supplier
import kotlin.reflect.KClass

object CypherBehaviorHooks {
    val RESOURCE_KEY: ResourceKey<Registry<HookModule<*>>> =
        ResourceKey.createRegistryKey(CypherNexus.modResource("cypher/hook"))
    val REGISTRY: Registry<HookModule<*>> = RegistryBuilder(RESOURCE_KEY).sync(true).create()

    val DEFERRED_REGISTER: DeferredRegister<HookModule<*>> =
        DeferredRegister.create(REGISTRY, CypherNexus.MOD_ID)

    fun register() {
        DEFERRED_REGISTER.register(MOD_BUS)
    }

    fun <T : Any> registerHook(path: String, hook: KClass<T>, target: HookType, sync: Boolean): Supplier<out HookModule<T>> {
        // checking DeferredHolder<R, T>'s type is annoying...
        return DEFERRED_REGISTER.register(path) { resource -> HookModule(resource, hook, sync = sync, type = target) }
    }
    fun <T : Any> registerHook(module: HookModule<T>): Supplier<out HookModule<T>> {
        return DEFERRED_REGISTER.register(module.resource.path) { resource -> module }
    }

    val INVOKE_REDIRECT_POS_SERVER = registerHook(ServerInvokeRedirectPosHook.MODULE)

    val HIT_ENTITY_SERVER = registerHook(ServerHitEntityHook.MODULE)
    val BEFORE_DISCARD_BOTH = registerHook(BothBeforeDiscardHook.MODULE)
    val FIRST_TICK_BOTH = registerHook(BothFirstTickHook.MODULE)
    val TICK_BEHAVIOR_BOTH = registerHook(BothTickBehaviorHook.MODULE)
}