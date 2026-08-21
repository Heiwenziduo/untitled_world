package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.DiscardReason
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotState
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.TriggerType
import com.github.nahnullscience.cypher_nexus.utility.linear_space.AnchoredCoordinate
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3

interface ICypherEntityPhysics {

    val tickStartSpeedSqr: Double
    val capturedInitialSpeedSqr: Double

    val triggerType: TriggerType
    val payload: ShotState?

    /**
     * store bounce points triggered in one tick
     * */
    val bouncePoints: List<Vec3>
    val bouncedThisTick: Boolean
    val canBounce: Boolean

    /**
     *
     * */
    fun trigger(coordinate: AnchoredCoordinate)

    /**
     *
     * */
    fun discardCypher(reason: DiscardReason)

    /**
     *
     * */
    fun explode(level: Level, x: Double, y: Double, z: Double, factor: Float = 1f): Boolean

    /**
     * should call inside Entity#tick, this handles all cypher-related logic
     * */
    fun doTick()
}