package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.DiscardReason
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookContainer
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HooksSharedData
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileNode
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.TriggerType
import com.github.nahnullscience.cypher_nexus.utility.EntityUtil.rotateTowardSpeed
import com.github.nahnullscience.cypher_nexus.utility.i.IFlagExtension
import com.github.nahnullscience.cypher_nexus.utility.mod.MapOfCypherCounts
import com.github.nahnullscience.cypher_nexus.utility.mod.PosDirePair
import net.minecraft.core.Direction
import net.minecraft.core.Holder
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.TraceableEntity
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
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
interface ICypherEntity : TraceableEntity, IFlagExtension, ICypherEntityBeforeInit {
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

        fun ICypherEntity.exertDamage(level: ServerLevel, target: Entity) {
            var damage = getAttributeOrDefault(CypherAttributes.DAMAGE)
            var crit = getAttributeOrDefault(CypherAttributes.CRIT_CHANCE)
            var critMulti = (owner as? LivingEntity)?.let { 1.5 } ?: 1.5 // there is no CritMultiplier Attribute, why
            var t = 1
            while (crit > 1 && t++ < Int.MAX_VALUE) {
                crit -= 1
                critMulti *= 1.5
            }
            if (t > 1 || target.random.nextFloat() < crit) {
                damage *= critMulti
            }
            target.hurtServer(level, getDamageSource(), damage.toFloat())
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
    override fun initDirection(pair: PosDirePair)

    override fun getOwner(): Entity?
    fun setOwner(owner: Entity?)

    fun attributeMap(): Map<CypherAttribute, Double>
    fun hooks(): HookContainer?
    val hooksSharedData: HooksSharedData<*>
    fun hooksSharedData(): HooksSharedData<*>

    fun triggerType(): TriggerType
    fun payload(): ShotStateChunk?

    fun getDirectionInitial(): Vec3
    fun getPositionInitial(): Vec3

    // attributes access functions
    fun getAttribute(attr: CypherAttribute): Double?
    fun getAttribute(holer: Holder<CypherAttribute>): Double?
    /**
     * get value through entity-specific map > cypher default > attribute default
     * */
    fun getAttributeOrDefault(attr: CypherAttribute): Double
    /**
     * get value through entity-specific map > cypher default > attribute default
     * */
    fun getAttributeOrDefault(holer: Holder<CypherAttribute>): Double
    fun getAttrBaseOrNull(holder: Holder<CypherAttribute>): Double?
    fun getAttrBaseOrNull(attr: CypherAttribute): Double?
    //
    fun getExisting(): Int
    fun getBounce(): Int
    fun getGravityFactor(): Float
    fun getSpeedFactor(): Float
    fun getEffectRadius(): Float
    /**
     * used as a factor inside [rotateTowardSpeed],
     * the higher the faster the entity will rotate, to face the direction the deltaMovement is pointed at
     * */
    fun getRotationSpeed(): Float
    fun getUnderwaterSpeedFactor(): Float
    fun getInWallSpeedFactor(): Float
    fun getBounceSpeedPenalty(): Double
    fun needCaptureSurrounding(): Boolean
    /**
     *
     * */
    fun getDamageSource(): DamageSource

    /**
     * store bounce points triggered in one tick
     * */
    val bouncePoints: List<Vec3>
    val bouncedThisTick: Boolean
    val canBounce: Boolean

    /***/
    fun trigger(type: TriggerType, releaseTo: PosDirePair)

    /***/
    fun discardCypher(reason: DiscardReason)

    // hooks // TODO extensive refactor
    /** call on both sides, override friendly */
    fun beforeDiscard(reason: DiscardReason)
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