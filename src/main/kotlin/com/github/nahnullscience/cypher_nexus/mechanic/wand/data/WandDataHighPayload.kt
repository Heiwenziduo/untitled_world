package com.github.nahnullscience.cypher_nexus.mechanic.wand.data

import com.github.nahnullscience.cypher_nexus.utility.mod.CNCodecs
import com.github.nahnullscience.cypher_nexus.utility.mod.ArrayOfCyphers
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec

/**
 * holds the cypher list
 * */
data class WandDataHighPayload(val aoc: ArrayOfCyphers) {
    companion object {
        fun of(capacity: Int) = WandDataHighPayload(ArrayOfCyphers(capacity))

//        val SPELL_DATA_CODEC: Codec<AbstractCypher> = ResourceLocation.CODEC.xmap(
//            { id -> ModCyphers.REGISTRY.get(id) }, // How to read (String ID -> Cypher Object)
//            { cypher -> ModCyphers.REGISTRY.getKey(cypher) } // How to write (Cypher Object -> String ID)
//        )
//        val REGISTRY_DATA_CODEC: Codec<AbstractCypher> = ModCyphers.REGISTRY.byNameCodec() // registry wrapper has a built-in codec, same as above
//        val HIGH_PAYLOAD_DATA_CODEC: Codec<WandDataHighPayload> = RecordCodecBuilder.create { it.group(
//            REGISTRY_DATA_CODEC
//                .listOf() // take the base codec and turn it into a list codec
//                .fieldOf("cypherList") // the key name it will have in NBT/JSON data
//                .forGetter(WandDataHighPayload::cypherList)
//        ).apply(it, ::WandDataHighPayload) }

        // "When a player joins a server, the server and client synchronize their registries,
        // assigning a simple integer ID to every ResourceLocation." so when networking registered things, use this
//        val REGISTRY_DATA_STREAM: StreamCodec<RegistryFriendlyByteBuf, AbstractCypher> =
//            ByteBufCodecs.registry(ModCyphers.RESOURCE_KEY) // stream a single registry item
//
//        val HIGH_PAYLOAD_DATA_STREAM: StreamCodec<RegistryFriendlyByteBuf, WandDataHighPayload> =
//            REGISTRY_DATA_STREAM
//                .apply(ByteBufCodecs.list()) // make single registry items to a list
//                // from StreamCodec<RegistryFriendlyByteBuf, List<AbstractCypher>>
//                // to   StreamCodec<RegistryFriendlyByteBuf, WandDataHighPayload>
//                .map(
//                    ::WandDataHighPayload,
//                    WandDataHighPayload::cypherList
//                )

        val HIGH_PAYLOAD_DATA_CODEC: Codec<WandDataHighPayload> = RecordCodecBuilder.create { instance ->
            instance.group(
                CNCodecs.AOC_CODEC
                    .fieldOf("cypherArray")
                    .forGetter(WandDataHighPayload::aoc)
            ).apply(instance, ::WandDataHighPayload)
        }

        val HIGH_PAYLOAD_DATA_STREAM: StreamCodec<RegistryFriendlyByteBuf, WandDataHighPayload> =
            CNCodecs.AOC_STREAM.map(
                ::WandDataHighPayload,
                WandDataHighPayload::aoc
            )
    }
}