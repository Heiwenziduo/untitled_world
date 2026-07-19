package com.github.nahnullscience.cypher_nexus.init.mod

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookModule
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookModule.HookBuilder
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.invoking.ServerInvokePosRedirectionHook
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.invoking.ServerInvokeCaptureHook
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.BothBeforeDiscardHook
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.BothEntitySearchHook
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.BothTickMovementFinalizeHook
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.BothFirstTickHook
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.BothOnBounceHook
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.BothHitEntityHook
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.BothTickBehaviorHook
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.RegistryBuilder
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS
import java.util.function.Supplier

object CypherHooks {
    val RESOURCE_KEY: ResourceKey<Registry<HookModule<*>>> =
        ResourceKey.createRegistryKey(CypherNexus.modResource("cypher/hook"))
    val REGISTRY: Registry<HookModule<*>> = RegistryBuilder(RESOURCE_KEY).sync(true).create()

    val DEFERRED_REGISTER: DeferredRegister<HookModule<*>> =
        DeferredRegister.create(REGISTRY, CypherNexus.MOD_ID)

    fun register() {
        DEFERRED_REGISTER.register(MOD_BUS)
    }

    fun <T : Any> registerHook(module: HookModule<T>): Supplier<out HookModule<T>> {
        return DEFERRED_REGISTER.register(module.resource.path) { resource -> module }
    }

    fun <T : Any> registerHook(builder: HookBuilder<T>): Supplier<out HookModule<T>> {
        return DEFERRED_REGISTER.register(builder.resource.path) { resource -> builder.build() }
    }

    /*
     * ensure that none of the hook implementations modify the state-chunk,
     * which may cause every projectile of the same state-chunk be modified together and de-sync.
     * hooks should only modify the projectile itself who preforms the hook
     * */

    val INVOKE_POS_REDIRECTION_SERVER = registerHook(ServerInvokePosRedirectionHook.HOOK)
    val INVOKE_CAPTURE = registerHook(ServerInvokeCaptureHook.HOOK)

    val HIT_ENTITY_BOTH = registerHook(BothHitEntityHook.HOOK)
    val BEFORE_DISCARD_BOTH = registerHook(BothBeforeDiscardHook.HOOK)
    val ENTITY_SEARCH_BOTH = registerHook(BothEntitySearchHook.HOOK)
    val FIRST_TICK_BOTH = registerHook(BothFirstTickHook.HOOK)
    val TICK_BEHAVIOR_BOTH = registerHook(BothTickBehaviorHook.HOOK)
    val TICK_MOVEMENT_FINALIZE_BOTH = registerHook(BothTickMovementFinalizeHook.HOOK)
    val ON_BOUNCE_BOTH = registerHook(BothOnBounceHook.HOOK)
}