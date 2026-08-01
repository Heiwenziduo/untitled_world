package com.github.nahnullscience.cypher_nexus.init.mod

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.AbstractInvokingPattern
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.patterns.NoPattern
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.patterns.PlaneBifurcatedPattern
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.patterns.PlaneTrifurcatedPattern
import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.RegistryBuilder
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS

object InvokingPatterns {
    val RESOURCE_KEY: ResourceKey<Registry<AbstractInvokingPattern>> =
        ResourceKey.createRegistryKey(CypherNexus.modResource("invoking/pattern"))
    val REGISTRY: Registry<AbstractInvokingPattern> =
        RegistryBuilder(RESOURCE_KEY).defaultKey(NoPattern.resource).create()


    val DEFERRED_REGISTER: DeferredRegister<AbstractInvokingPattern> =
        DeferredRegister.create(REGISTRY, CypherNexus.MOD_ID)

    fun register() {
        DEFERRED_REGISTER.register(MOD_BUS)
    }

    fun registerPattern(pattern: AbstractInvokingPattern): Holder<AbstractInvokingPattern> {
        return DEFERRED_REGISTER.register(pattern.resource.path) { -> pattern }
    }

    fun registerPattern(path: String, supplier: (resource: Identifier) -> AbstractInvokingPattern): Holder<AbstractInvokingPattern> {
        return DEFERRED_REGISTER.register(path, supplier)
    }

    val NO_PATTERN = registerPattern(NoPattern)
    val PLANE_BIFURCATED_PATTERN = registerPattern("plane_bifurcated", ::PlaneBifurcatedPattern)
    val PLANE_TRIFURCATED_PATTERN = registerPattern("plane_trifurcated", ::PlaneTrifurcatedPattern)
//    val PLANE_T_STYLE_PATTERN = registerPattern("plane_t_style", ::PlaneTStylePattern)
}