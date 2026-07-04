package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileNode
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk
import com.github.nahnullscience.cypher_nexus.utility.mod.MapOfCypherCounts
import com.github.nahnullscience.cypher_nexus.utility.mod.PosDirePair
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3

/**
 * a safeguard interface,
 * defines properties that can be access safely before calling [ICypherEntity.initEntity].
 * properties inside this interface construct a subset of those inside [ICypherEntity]
 * */
interface ICypherBeforeInit {
    /**
     * [MapOfCypherCounts] serves as the token of [ShotStateChunk],
     * this field initialized in server and will be shipped to client to sync shot-data
     * */
    fun ccMap(): MapOfCypherCounts?
    /**
     * initialize from [MapOfCypherCounts]
     * */
    fun initCypher(cypher: AbstractProjectileCypher<*>, map: MapOfCypherCounts?)
    /**
     * init from [ShotStateChunk]
     * */
    fun initCypher(cypher: AbstractProjectileCypher<*>, state: ShotStateChunk, node: ProjectileNode?)
    /**
     *
     * */
    fun initDirection(pair: PosDirePair)
}