package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.DiscardReason
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.TriggerType
import com.github.nahnullscience.cypher_nexus.utility.CoordinateDefinition
import com.github.nahnullscience.cypher_nexus.utility.PosDirePair
import net.minecraft.world.phys.Vec3

interface ICypherEntityPhysics {
    val tickStartSpeedSqr: Double

    val triggerType: TriggerType
    val payload: ShotStateChunk?

    /**
     * store bounce points triggered in one tick
     * */
    val bouncePoints: List<Vec3>
    val bouncedThisTick: Boolean
    val canBounce: Boolean

    /**
     *
     * */
    fun trigger(coordinate: CoordinateDefinition, releaseTo: PosDirePair)

    /**
     *
     * */
    fun discardCypher(reason: DiscardReason)

    /**
     * should call inside Entity#tick, this handles all cypher-related logic
     * */
    fun doTick()
}