package com.github.nahnullscience.cypher_nexus.init

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.network.CNCodecs
import net.minecraft.network.syncher.EntityDataSerializer
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.NeoForgeRegistries
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS

/* @doc
 * Minecraft uses a hard-coded map of serializers.
 * NeoForge transforms this map into a registry,
 * meaning that if you want to add new EntityDataSerializers,
 * they must be added by registration.
 * */
object ModDataSerializers {

    val DEFERRED_REGISTER: DeferredRegister<EntityDataSerializer<*>> =
        DeferredRegister.create(NeoForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS, CypherNexus.MOD_ID)

    fun register() {
        DEFERRED_REGISTER.register(MOD_BUS)
    }

    // NOTE set ".sync(true)" on custom REGISTRY, to allow registry-object pass through network
    val CYPHER_DATA: DeferredHolder<EntityDataSerializer<*>, EntityDataSerializer<AbstractCypher>> =
        DEFERRED_REGISTER.register("cypher_data", )
        { t -> EntityDataSerializer.forValueType(CNCodecs.CYPHER_STREAM) }

    val CYPHER_LIST_DATA: DeferredHolder<EntityDataSerializer<*>, EntityDataSerializer<List<AbstractCypher>>> =
        DEFERRED_REGISTER.register("cypher_list_data", )
        { t -> EntityDataSerializer.forValueType(CNCodecs.CYPHER_LIST_STREAM) }

    // TODO
    val HOOK_CONTAINER = null
}