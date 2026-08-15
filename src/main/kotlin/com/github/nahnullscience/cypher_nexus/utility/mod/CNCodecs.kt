package com.github.nahnullscience.cypher_nexus.utility.mod

import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.init.mod.CypherSteerers
import com.github.nahnullscience.cypher_nexus.init.mod.Cyphers
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.AttributeFastMap
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.AttributeFastOperatorMap
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.AttributeOperator
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.AttributeOperator.Companion.string2operator
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.steerer.AbstractCypherSteerer
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.netty.buffer.ByteBuf
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import java.util.*

object CNCodecs {

    val UUID_CODEC: Codec<UUID> = RecordCodecBuilder.create { it.group(
        Codec.LONG.fieldOf("msb").forGetter(UUID::getMostSignificantBits),
        Codec.LONG.fieldOf("lsb").forGetter(UUID::getLeastSignificantBits)
    ).apply(it) { m, l -> UUID(m, l) } }
    val UUID_STREAM: StreamCodec<ByteBuf, UUID> = StreamCodec.composite(
        ByteBufCodecs.VAR_LONG, UUID::getMostSignificantBits,
        ByteBufCodecs.VAR_LONG, UUID::getLeastSignificantBits,
        ::UUID
    )

//    val DOUBLE_ARRAY_CODEC: Codec<DoubleArray> = Codec.DOUBLE.listOf().xmap(
//        { list -> DoubleArray(list.size) { i -> list[i] } },
//        { doubles -> buildList(doubles.size) { for (i in doubles.indices) add(i, doubles[i]) } }
//    )


    val CYPHER: Codec<AbstractCypher> = Cyphers.REGISTRY.byNameCodec()
    val CYPHER_STREAM: StreamCodec<RegistryFriendlyByteBuf, AbstractCypher> =
        ByteBufCodecs.registry(Cyphers.RESOURCE_KEY)

    val CYPHER_LIST: Codec<List<AbstractCypher>> = CYPHER.listOf()
    val CYPHER_LIST_STREAM: StreamCodec<RegistryFriendlyByteBuf, List<AbstractCypher>> =
        CYPHER_STREAM.apply(ByteBufCodecs.list())

    val CYPHER_ATTRIBUTE: Codec<CypherAttribute> = CypherAttributes.REGISTRY.byNameCodec()

    val CYPHER_STEERER: Codec<AbstractCypherSteerer> = CypherSteerers.REGISTRY.byNameCodec()
    val CYPHER_STEERER_STREAM: StreamCodec<RegistryFriendlyByteBuf, AbstractCypherSteerer> =
        ByteBufCodecs.registry(CypherSteerers.RESOURCE_KEY)


    val AOC_CODEC: Codec<ArrayOfCyphers> = CYPHER_LIST.xmap(
        { list -> ArrayOfCyphers(list) },
        { aoc -> aoc.toList() }
    )
    val AOC_STREAM: StreamCodec<RegistryFriendlyByteBuf, ArrayOfCyphers> = CYPHER_LIST_STREAM.map(
        { list -> ArrayOfCyphers(list) },
        { aoc -> aoc.toList() }
    )

    val MOCC_CODEC: Codec<MapOfCypherCounts> =
        Codec.unboundedMap(CYPHER, Codec.INT).xmap(
            { map -> MapOfCypherCounts(map) },
            { mocc -> mocc }
        )
    val MOCC_STREAM: StreamCodec<RegistryFriendlyByteBuf, MapOfCypherCounts> =
        ByteBufCodecs.map(
            { capa -> MapOfCypherCounts(capa) },
            CYPHER_STREAM, ByteBufCodecs.VAR_INT
        )

    val ATTR_OPERATOR_CODEC: Codec<AttributeOperator> = Codec.STRING.comapFlatMap(
        // Given a string codec to convert to a integer
        // Not all strings can become integers (A is not fully equivalent to B)
        // All integers can become strings (B is fully equivalent to A)
        { s ->
            try {
                return@comapFlatMap DataResult.success(string2operator(s))
            } catch (e: IllegalArgumentException) {
                return@comapFlatMap DataResult.error { "$s is not a valid operator;\n$e" }
            }
        },
        AttributeOperator::toString
    )
    /** represents a map that key is string-fied attribute-operator, and value is a double */
    val ATTR_OPERATOR_MAP_CODEC: Codec<EnumMap<AttributeOperator, Double>> =
        Codec.unboundedMap(ATTR_OPERATOR_CODEC, Codec.DOUBLE).comapFlatMap(
            { map ->
                try {
                    return@comapFlatMap DataResult.success(EnumMap(map))
                } catch (e: IllegalArgumentException) {
                    return@comapFlatMap DataResult.error { "$map is not a valid operator-map;\n$e" }
                }
            },
            { enumMap -> enumMap }
        )

    val ATTR_FAST_MAP_CODEC: Codec<AttributeFastMap> =
        Codec.unboundedMap(CYPHER_ATTRIBUTE, Codec.DOUBLE).xmap(
            { map -> AttributeFastMap(map) },
            { afm -> afm.toMap() }
        )

    val ATTR_FAST_OPER_MAP_CODEC: Codec<AttributeFastOperatorMap> =
        Codec.unboundedMap(CYPHER_ATTRIBUTE, ATTR_OPERATOR_MAP_CODEC).xmap(
            { map -> AttributeFastOperatorMap(map) },
            { fastOpMap -> fastOpMap.toEnumMap() }
        )
}