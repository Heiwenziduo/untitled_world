package com.github.nahnullscience.cypher_nexus.client.cypher

import com.github.nahnullscience.cypher_nexus.client.cypher.visualizer.*
import com.github.nahnullscience.cypher_nexus.content.cypher.projectile.EnderRecallCypher
import com.github.nahnullscience.cypher_nexus.init.mod.Cyphers
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import net.minecraft.resources.ResourceLocation
import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn

@OnlyIn(Dist.CLIENT)
object CypherVisualizerRegistry {
    private val _visualizers = mutableMapOf<AbstractCypher, ICypherVisualizer>()

    // call this in FMLClientSetupEvent
    fun register(visualizer: ICypherVisualizer) {
        _visualizers[visualizer.cypher()] = visualizer
    }
    fun register(cypher: AbstractCypher, visualizer: ICypherVisualizer) {
        _visualizers[cypher] = visualizer
    }

    fun get(cypherId: ResourceLocation): ICypherVisualizer? {
        val cy = Cyphers.REGISTRY.get(cypherId)
        return _visualizers[cy]
    }
    fun get(cypher: AbstractCypher): ICypherVisualizer? {
        return _visualizers[cypher]
    }

    fun init() {

        register(SnowballVi)
        register(EnderTeleportationVi)
        register(EnderRecallCypher, EnderTeleportationVi)
        register(ArrowVi)
        register(LlamaSpitVi)
        register(SpawnEggVi)

    }
}