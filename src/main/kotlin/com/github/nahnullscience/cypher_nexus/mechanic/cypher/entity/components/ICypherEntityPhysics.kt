package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.DiscardReason
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.TriggerType
import com.github.nahnullscience.cypher_nexus.utility.PosDirePair
import net.minecraft.core.Direction
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3

interface ICypherEntityPhysics {

    val triggerType: TriggerType
    val payload: ShotStateChunk?

    /**
     * store bounce points triggered in one tick
     * */
    val bouncePoints: List<Vec3>
    val bouncedThisTick: Boolean
    val canBounce: Boolean



    /***/
    fun discardCypher(reason: DiscardReason)

    /**
     * should call inside Entity#tick, this handles all cypher-related logic
     * */
    fun doTick()
    /***/
    fun trigger(type: TriggerType, releaseTo: PosDirePair)
    /**
     * use as general entity selector through [net.minecraft.world.level.Level.getEntities]
     * */
    fun canHitTarget(target: Entity): Boolean
    /**
     * when the entity "hit" something,
     * both [net.minecraft.world.phys.EntityHitResult] and [net.minecraft.world.phys.BlockHitResult]
     * will be passed into this method.
     *
     * this method is called on both sides
     * */
    fun whenHit(result: HitResult, direction: Direction)
    fun whenHitEntity(result: EntityHitResult, direction: Direction)
    fun whenHitBlock(result: BlockHitResult, direction: Direction)
    /**
     *
     * */
    fun canHomeTarget(target: Entity): Boolean
    /**
     *
     * */
    fun whileHomeTarget(target: Entity)
}