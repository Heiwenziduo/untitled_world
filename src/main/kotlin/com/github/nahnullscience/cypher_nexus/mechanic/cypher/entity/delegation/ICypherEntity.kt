package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.DiscardReason
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookContainer
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HooksSharedData
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileNode
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.TriggerType
import com.github.nahnullscience.cypher_nexus.utility.EntityUtil.rotateTowardSpeed
import com.github.nahnullscience.cypher_nexus.utility.i.IFlagExtension
import com.github.nahnullscience.cypher_nexus.utility.mod.MapOfCypherCounts
import com.github.nahnullscience.cypher_nexus.utility.mod.PosDirePair
import net.minecraft.core.Holder
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.TraceableEntity
import net.minecraft.world.entity.animal.Animal
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import net.neoforged.bus.api.EventPriority
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent

/**
 * define data pieces that all cypher-entity would require.
 * those pieces then could be delivered through Delegation
 *
 * ONLY methods start with prefix "get" can be overridden in respective entity classes,
 * overriding other method have no effect.
 * */
interface ICypherEntity : TraceableEntity, IFlagExtension, ICypherBeforeInit {
    @EventBusSubscriber(modid = CypherNexus.MOD_ID)
    companion object {
        const val CLIP_MARGIN = 0.2f
        const val CAPTURE_SIZE = 8.0
        const val CAPTURE_SIZE_SQR = CAPTURE_SIZE * CAPTURE_SIZE
        const val LOW_SPEED_THRESHOLD = 0.02
        const val LOW_SPEED_THRESHOLD_SQR = LOW_SPEED_THRESHOLD * LOW_SPEED_THRESHOLD
        const val HIT_BB_INFLATION = 0.25

        @SubscribeEvent(priority = EventPriority.NORMAL)
        private fun initCypherEntity(event: EntityJoinLevelEvent) {
            val entity = event.entity
            if (entity is ICypherEntity) {
                entity.initEntity(entity)
            }
        }
    }

    val cypherHolder: Holder<out AbstractProjectileCypher<*>>
    val cypher get() = cypherHolder.value()

    /**
     * [MapOfCypherCounts] serves as the token of [ShotStateChunk],
     * this field initialized in server and will be shipped to client to sync shot-data
     * */
    override fun ccMap(): MapOfCypherCounts?
    /**
     * initialize from [MapOfCypherCounts]
     * */
    override fun initCypher(cypher: AbstractProjectileCypher<*>, map: MapOfCypherCounts?)
    /**
     * init from [ShotStateChunk]
     * */
    override fun initCypher(cypher: AbstractProjectileCypher<*>, state: ShotStateChunk, node: ProjectileNode?)
    /**
     *
     * */
    fun <E> initEntity(cy: E) where E : Entity, E : ICypherEntity
    /**
     *
     * */
    override fun initDirection(direction: Vec3?)
    /**
     *
     * */
    override fun initDirection(pair: PosDirePair)

    //    val attributeMap: Map<CypherAttribute, Double>
    fun attributeMap(): Map<CypherAttribute, Double>

//    val hooks: HookContainer?
    fun hooks(): HookContainer?
    val hooksSharedData: HooksSharedData<*>
    fun hooksSharedData(): HooksSharedData<*>

    fun triggerType(): TriggerType
    fun payload(): ShotStateChunk?

//    val trigger: TriggerType
//    val payload: ShotStateChunk?

    // quick access for common attributes
//    val directionInitial: Vec3
    fun getDirectionInitial(): Vec3
//    val positionInitial: Vec3
    fun getPositionInitial(): Vec3
//    var existing: Int
    fun getExisting(): Int
    fun getSpeed(): Double
    fun getBounce(): Int
//    fun canBounce(): Boolean
//    val speed: Double get() = getAttrOrProjDefault(CypherAttributes.SPEED)
//    val bounce: Int get() = getAttrOrProjDefault(CypherAttributes.BOUNCE).toInt()
    fun getGravityFactor(): Float
    fun getSpeedFactor(): Float
    /**
     * used as a factor inside [rotateTowardSpeed],
     * the higher the faster the entity will rotate, to face the direction the deltaMovement is pointed at
     * */
    fun getRotationSpeed(): Float
    fun getEffectRadius(): Double
    /**
     * store bounce points triggered in one tick
     * */
    val bouncePoints: List<Vec3>
    val bouncedThisTick: Boolean
    val canBounce: Boolean
//    val gravity: Float get() = getAttrOrProjDefault(CypherAttributes.GRAVITY_FACTOR).toFloat()
//    val speedFactor: Float get() = 1f - getAttrOrProjDefault(CypherAttributes.FRICTION_FACTOR).toFloat()
//    val rotationSpeed: Float
//    val effectRadius: Double get() = getAttrOrProjDefault(CypherAttributes.EFFECT_RADIUS)


    // attributes access functions
    fun attribute(attr: CypherAttribute): Double?
    fun attribute(holer: Holder<CypherAttribute>): Double?
    /**
     * get value through entity-specific map > cypher default > attribute default
     * */
    fun attributeOrDefault(attr: CypherAttribute): Double
    /**
     * get value through entity-specific map > cypher default > attribute default
     * */
    fun attributeOrDefault(holer: Holder<CypherAttribute>): Double

    //
    fun needCaptureSurrounding(): Boolean
    fun getUnderwaterSpeedFactor(): Float
    fun getInWallSpeedFactor(): Float
    fun getBounceSpeedPenalty(): Double

    fun trigger(type: TriggerType)

    fun discardCypher(reason: DiscardReason)

    // hooks // TODO extensive refactor
    fun beforeDiscardBoth(reason: DiscardReason)
    fun hitBoth(result: HitResult)
    fun firstTickBoth()
    fun tickBehaviorBoth()
    fun tickFinalizeMovementBoth()
    fun bounceBoth(bouncePoint: Vec3)
    fun captureSurroundingBoth(captured: Entity)
    fun lowSpeedBoth(count: Int)

    /**
     * should call inside Entity#tick, this handles all cypher-related logic
     * */
    fun doTick()
    /**
     * use as general entity selector through [net.minecraft.world.level.Level.getEntities]
     * */
    fun canHitTarget(target: Entity): Boolean
    /**
     * when the entity "hit" something,
     * both [net.minecraft.world.phys.EntityHitResult] and [net.minecraft.world.phys.BlockHitResult]
     * will be passed into this method. this method is called on both sides
     * */
    fun whenHit(result: HitResult)
    /**
     *
     * */
    fun canHomeTarget(target: Entity): Boolean
    /**
     *
     * */
    fun whileHomeTarget(target: Entity)

    override fun getOwner(): Entity?
    fun setOwner(owner: Entity?)
}