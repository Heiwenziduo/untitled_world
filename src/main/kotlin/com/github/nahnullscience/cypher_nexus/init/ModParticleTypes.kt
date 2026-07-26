package com.github.nahnullscience.cypher_nexus.init

import com.github.nahnullscience.cypher_nexus.CypherNexus
import net.minecraft.core.particles.ParticleType
import net.minecraft.core.particles.SimpleParticleType
import net.minecraft.core.registries.BuiltInRegistries
import net.neoforged.neoforge.registries.DeferredRegister
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS
import java.util.function.Supplier


object ModParticleTypes {
    val DEFERRED_REGISTER: DeferredRegister<ParticleType<*>> =
        DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, CypherNexus.MOD_ID)

    fun register() {
        DEFERRED_REGISTER.register(MOD_BUS)
    }

    private fun registerParticle(path: String, unlimited: Boolean = true): Supplier<SimpleParticleType> {
        // The easiest way to add new particle types is reusing vanilla's SimpleParticleType.
        // extend ParticleType & ParticleOptions when you want to ship custom information across network
        return DEFERRED_REGISTER.register<SimpleParticleType>(path) { -> SimpleParticleType(unlimited) }
    }

//    val CYPHER_TRAIL = registerParticle("cypher_trail")
    val DISTANCE_INVOKE_TRAIL = registerParticle("distance_invoke_invoke")
}