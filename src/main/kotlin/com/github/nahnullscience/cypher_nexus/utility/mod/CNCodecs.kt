package com.github.nahnullscience.cypher_nexus.utility.mod

import com.github.nahnullscience.cypher_nexus.init.mod.Cyphers
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookContainer
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.netty.buffer.ByteBuf
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

object CNCodecs {
    val CYPHER: Codec<AbstractCypher> = Cyphers.REGISTRY.byNameCodec()
    val CYPHER_STREAM: StreamCodec<RegistryFriendlyByteBuf, AbstractCypher> = ByteBufCodecs.registry(Cyphers.RESOURCE_KEY)

    val CYPHER_LIST: Codec<List<AbstractCypher>> = CYPHER.listOf()
    val CYPHER_LIST_STREAM: StreamCodec<RegistryFriendlyByteBuf, List<AbstractCypher>> = CYPHER_STREAM.apply(
        ByteBufCodecs.list())

    val AOC_CODEC: Codec<ArrayOfCyphers> = CYPHER_LIST.xmap(
        { list -> ArrayOfCyphers(list) },
        { aoc -> aoc.toList() }
    )
    val AOC_STREAM: StreamCodec<RegistryFriendlyByteBuf, ArrayOfCyphers> = CYPHER_LIST_STREAM.map(
        { list -> ArrayOfCyphers(list) },
        { aoc -> aoc.toList() }
    )

    val HOOK_CONTAINER: Codec<HookContainer> = Codec.recursive(HookContainer::class.simpleName) {
        recursedCodec -> RecordCodecBuilder.create()
        {
            it.group(
                recursedCodec.optionalFieldOf("parent").forGetter(HookContainer::parent)
            ).apply(it, ::HookContainer)
        }
    }
    val HOOK_CONTAINER_STREAM: StreamCodec<ByteBuf, HookContainer> = StreamCodec.recursive {
        recursedStreamCodec -> StreamCodec.composite(
            recursedStreamCodec.apply(ByteBufCodecs::optional),
            HookContainer::parent,
            ::HookContainer
        )
    }
}