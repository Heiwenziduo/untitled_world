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
     * store bounce points triggered in one tick,
     * currently this only present on client side.
     * */
    val bouncePoints: BouncePointsManager?
    val bouncedThisTick: Boolean

    /**
     *
     * */
    fun trigger(coordinate: AnchoredCoordinate)

    /**
     * can be called on both side
     * */
    fun discardCypher(reason: DiscardReason)

    /**
     * can be called on both side
     * */
    fun explode(level: Level, x: Double, y: Double, z: Double, factor: Float = 1f): Boolean

    /**
     * should call inside Entity#tick, this handles all cypher-related logic
     * */
    fun doTick()
}
