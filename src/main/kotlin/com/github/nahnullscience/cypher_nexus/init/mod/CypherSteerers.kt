package com.github.nahnullscience.cypher_nexus.init.mod

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.LifeCycle.getIdOfBound
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.createCypherEntityRaw
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.steerer.AbstractCypherSteerer
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.steerer.EscortOrbitSteerer
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.steerer.NoSteerer
import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.RegistryBuilder
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS

/**
 * steerer is a simple logic component that controls a cypher-entity's behavior externally.
 *
 * main use case is CEs come from [createCypherEntityRaw].
 * they don't have backing shot states, so a steerer is required to perform variable behaviors.
 *
 * it should be stateless to avoid instantiate overhead.
 * */
object CypherSteerers {
    const val STEER_ID_CAP = 31
    val RESOURCE_KEY: ResourceKey<Registry<AbstractCypherSteerer>> =
        ResourceKey.createRegistryKey(CypherNexus.modResource("cypher/steerer"))
    val REGISTRY: Registry<AbstractCypherSteerer> =
        RegistryBuilder(RESOURCE_KEY).defaultKey(NoSteerer.resource).sync(true).maxId(STEER_ID_CAP).create()

    fun AbstractCypherSteerer.id(): Int = REGISTRY.getIdOfBound(this, STEER_ID_CAP)

    val DEFERRED_REGISTER: DeferredRegister<AbstractCypherSteerer> =
        DeferredRegister.create(REGISTRY, CypherNexus.MOD_ID)

    fun register() {
        DEFERRED_REGISTER.register(MOD_BUS)
    }

    fun registerSteerer(steerer: AbstractCypherSteerer): Holder<AbstractCypherSteerer> =
        DEFERRED_REGISTER.register(steerer.resource.path) { resource -> steerer }

    fun registerSteerer(path: String, factory: (Identifier) -> AbstractCypherSteerer): Holder<AbstractCypherSteerer> =
        DEFERRED_REGISTER.register(path, factory)

    val NO_STEERER = registerSteerer(NoSteerer)
    val ESCORT_ORBIT_STEERER = registerSteerer("escort_orbit", ::EscortOrbitSteerer)
}