package com.github.nahnullscience.cypher_nexus.datagen.client

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.ModParticleTypes
import net.minecraft.data.PackOutput
import net.minecraft.resources.Identifier
import net.neoforged.neoforge.client.data.ParticleDescriptionProvider

class CNParticleDescriptionProvider(output: PackOutput) : ParticleDescriptionProvider(output) {
    override fun addDescriptions() {
        // Adds a single sprite particle definition with the file at
        // assets/examplemod/textures/particle/my_single_particle.png.
        spriteSet(ModParticleTypes.DISTANCE_INVOKE_TRAIL.get(), CypherNexus.modResource("distance_invoke_invoke"))
        // Adds a multi sprite particle definition, with a vararg parameter. Alternatively accepts an iterable.

        val l = mutableListOf<Identifier>()
//        for (i in 0..9) l.add(CypherNexus.modResource("cypher_trail_$i"))
//        spriteSet(ModParticleTypes.CYPHER_TRAIL.get(), l)

    }
}