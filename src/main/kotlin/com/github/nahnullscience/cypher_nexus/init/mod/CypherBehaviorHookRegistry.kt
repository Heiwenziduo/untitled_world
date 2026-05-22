package com.github.nahnullscience.cypher_nexus.init.mod

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookModule
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookModule.HookType
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookModule.HookType.INVOKING
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookModule.HookType.PROJECTILE
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.invoking.HookInvokeRedirectPosServer
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.HookBeforeDiscardBoth
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.HookFirstTickBoth
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.HookHitEntityServer
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.HookTickBehaviorBoth
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

    fun <T : Any> registerHook(path: String, hook: KClass<T>, target: HookType, sync: Boolean): Supplier<out HookModule<T>> {
        // checking DeferredHolder<R, T>'s type is annoying...
        return DEFERRED_REGISTER.register(path) { resource -> HookModule(resource, hook, sync = sync, type = target) }
    }

    val INVOKE_REDIRECT_POS_SERVER = registerHook("invoke_redirect_pos", HookInvokeRedirectPosServer::class, INVOKING, false)

    val HIT_ENTITY_SERVER = registerHook("hit_entity", HookHitEntityServer::class, PROJECTILE, false)
    val BEFORE_DISCARD_BOTH = registerHook("before_discard", HookBeforeDiscardBoth::class, PROJECTILE, true)
    val FIRST_TICK_BOTH = registerHook("first_tick", HookFirstTickBoth::class, PROJECTILE, true)
    val TICK_BEHAVIOR_BOTH = registerHook("tick_behavior", HookTickBehaviorBoth::class, PROJECTILE, true)
}