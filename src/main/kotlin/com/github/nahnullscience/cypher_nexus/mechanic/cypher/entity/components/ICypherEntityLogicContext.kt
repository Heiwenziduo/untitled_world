package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.DiscardReason
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.steerer.AbstractCypherSteerer
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookContainer
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HooksSharedData
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk
import com.github.nahnullscience.cypher_nexus.utility.i.IFlagExtension
import com.github.nahnullscience.cypher_nexus.utility.mod.MapOfCypherCounts
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.TraceableEntity
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3

interface ICypherEntityLogicContext : TraceableEntity, IFlagExtension {

    /**
     * [MapOfCypherCounts] serves as the token of [ShotStateChunk],
     * this field initialized in server and will be shipped to client to sync shot-data
     *
     * this field directly forwards to the backing [ShotStateChunk] and should be treated as `immutable`
     * */
    val ccMap: MapOfCypherCounts?

    /** this field directly forwards to the backing [ShotStateChunk] and should be treated as `immutable` */
    val hooks: HookContainer?
    val hooksSharedData: HooksSharedData<*>
    val steerer: AbstractCypherSteerer

    /** tint from dyes */
    val hue: Int?
    /** 0f~1f float representation of [hue], in order of r0 g1 b2 a3 */
    val hueFloatArray: FloatArray?

    override fun getOwner(): Entity?
    fun setOwner(owner: Entity?)

    // hooks // TODO extensive refactor
    /** call on both sides, override friendly */
    fun <CE> beforeDiscard(reason: DiscardReason)
    /** call on both sides, override friendly */
    fun onHit(result: HitResult)
    /** call on both sides, override friendly */
    fun onFirstTick()
    /** call on both sides, override friendly */
    fun onTick()
    /** call on both sides, override friendly */
    fun finalizeTickMovement()
    /** call on both sides, override friendly */
    fun onBounce(bouncePoint: Vec3)
    /** call on both sides, override friendly */
    fun forEntityCaptured(captured: Entity)
    /** call on both sides, override friendly */
    fun onLowSpeed(count: Int)
//    /** only on Server */
//    fun onDealDamage(damage: Double)
    /**
     *
     * */
    fun getDamageSource(): DamageSource
}