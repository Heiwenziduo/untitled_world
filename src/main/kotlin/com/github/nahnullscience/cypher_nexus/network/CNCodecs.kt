package com.github.nahnullscience.cypher_nexus.network

import com.github.nahnullscience.cypher_nexus.init.mod.ModCyphers
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.mojang.serialization.Codec
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

object CNCodecs {
    val CYPHER: Codec<AbstractCypher> = ModCyphers.REGISTRY.byNameCodec()
    val CYPHER_STREAM: StreamCodec<RegistryFriendlyByteBuf, AbstractCypher> = ByteBufCodecs.registry(ModCyphers.RESOURCE_KEY)

    val CYPHER_LIST: Codec<List<AbstractCypher>> = CYPHER.listOf()
    val CYPHER_LIST_STREAM: StreamCodec<RegistryFriendlyByteBuf, List<AbstractCypher>> = CYPHER_STREAM.apply(ByteBufCodecs.list())
}