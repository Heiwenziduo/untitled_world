package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileNode
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk
import com.github.nahnullscience.cypher_nexus.utility.mod.MapOfCypherCounts
import com.github.nahnullscience.cypher_nexus.utility.mod.PosDirePair

/**
 * a safeguard interface,
 * defines properties that can be access safely before calling [ICypherEntity.initEntity].
 * properties inside this interface construct a subset of those inside [ICypherEntity]
 * */
interface ICypherEntityBeforeInit {
    /**
     * [MapOfCypherCounts] serves as the token of [ShotStateChunk],
     * this field initialized in server and will be shipped to client to sync shot-data
     * */
    val ccMap: MapOfCypherCounts?
    /**
     * initialize from [MapOfCypherCounts]
     * */
    fun initCypher(cypher: AbstractProjectileCypher<*>, ccMap: MapOfCypherCounts?)
    /**
     * init from [ShotStateChunk]
     * */
    fun initCypher(cypher: AbstractProjectileCypher<*>, shotState: ShotStateChunk, node: ProjectileNode?)
    /**
     *
     * */
    fun initDirection(pair: PosDirePair)
}