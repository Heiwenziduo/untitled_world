package com.github.nahnullscience.cypher_nexus.init.mod

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.LifeCycle.getIdOfBound
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookModule
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookModule.HookBuilder
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.IHook
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.invoking.ServerInvokeAbortReleaseHook
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.invoking.ServerInvokePosRedirectionHook
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.invoking.ServerInvokeSurroundingCaptureHook
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.*
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.RegistryBuilder
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS
import java.util.function.Supplier

object CypherHooks {
    const val HOOK_ID_CAP = 31
    val RESOURCE_KEY: ResourceKey<Registry<HookModule<*>>> =
        ResourceKey.createRegistryKey(CypherNexus.modResource("cypher/hook"))
    val REGISTRY: Registry<HookModule<*>> =
        RegistryBuilder(RESOURCE_KEY).sync(true).maxId(HOOK_ID_CAP).create()

    fun HookModule<*>.id(): Int = REGISTRY.getIdOfBound(this, HOOK_ID_CAP)

    val DEFERRED_REGISTER: DeferredRegister<HookModule<*>> =
        DeferredRegister.create(REGISTRY, CypherNexus.MOD_ID)

    fun register() {
        DEFERRED_REGISTER.register(MOD_BUS)
    }

    fun <T : IHook> registerHook(module: HookModule<T>): Supplier<out HookModule<T>> {
        return DEFERRED_REGISTER.register(module.resource.path) { resource -> module }
    }

    fun <T : IHook> registerHook(builder: HookBuilder<T>): Supplier<out HookModule<T>> {
        return DEFERRED_REGISTER.register(builder.resource.path) { resource -> builder.build() }
    }



    // invoking
    val INVOKE_ABORT_RELEASE_SERVER = registerHook(ServerInvokeAbortReleaseHook.HOOK)
    val INVOKE_POS_REDIRECTION_SERVER = registerHook(ServerInvokePosRedirectionHook.HOOK)
    val INVOKE_CAPTURE_SERVER = registerHook(ServerInvokeSurroundingCaptureHook.HOOK)

    // behavior
    val BEFORE_DISCARD_SERVER = registerHook(ServerBeforeDiscardHook.HOOK)

    val ENTITY_CAPTURE = registerHook(EntityCaptureHook.HOOK)
    val FIRST_TICK = registerHook(FirstTickHook.HOOK)
    val GENERAL_HIT = registerHook(GeneralOnHitHook.HOOK)
    val ON_BOUNCE = registerHook(OnBounceHook.HOOK)
    val ON_EXPLODE = registerHook(OnExplodeHook.HOOK)
    val TICK_BEHAVIOR = registerHook(TickBehaviorHook.HOOK)
    val TICK_MOVEMENT_FINALIZE = registerHook(TickMovementFinalizeHook.HOOK)
}