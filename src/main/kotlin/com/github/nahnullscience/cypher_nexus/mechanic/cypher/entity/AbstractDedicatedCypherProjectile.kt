package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation.CypherEntityDelegation
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity.Companion.cypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntityAttributeAccessor.Companion.getEffectRadius
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntityAttributeAccessor.Companion.getExisting
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import com.github.nahnullscience.cypher_nexus.utility.centeredAABB
import com.github.nahnullscience.cypher_nexus.utility.i.IFlagExtension
import com.github.nahnullscience.cypher_nexus.utility.mod.CNCodecs.CYPHER_STEERER_STREAM
import com.github.nahnullscience.cypher_nexus.utility.mod.CNCodecs.MOCC_STREAM
import com.github.nahnullscience.cypher_nexus.utility.sideString
import net.minecraft.core.Holder
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityDimensions
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.Pose
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Explosion
import net.minecraft.world.level.Level
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn
import java.util.*
import java.util.function.Consumer

abstract class AbstractDedicatedCypherProjectile(
    entityType: EntityType<out AbstractDedicatedCypherProjectile>,
    level: Level
) : Projectile(entityType, level), IEntityWithComplexSpawn,
    IFlagExtension, ICypherEntity by CypherEntityDelegation<AbstractDedicatedCypherProjectile>() {
    companion object {

    }

    override fun defineSynchedData(builder: SynchedEntityData.Builder) { }
    override fun onSyncedDataUpdated(key: EntityDataAccessor<*>) {
        super.onSyncedDataUpdated(key)
    }
    override fun readAdditionalSaveData(input: ValueInput) = Unit
    override fun addAdditionalSaveData(output: ValueOutput) = Unit

    override fun writeSpawnData(buffer: RegistryFriendlyByteBuf) {
        // send when entity added to level
        buffer.writeBoolean(ccMap != null) // write & read relay strictly on order, use a marker to tell client if a map follows
        if (ccMap != null) {
            MOCC_STREAM.encode(buffer, ccMap!!)
        }
        CYPHER_STEERER_STREAM.encode(buffer, steerer)
    }

    override fun readSpawnData(buffer: RegistryFriendlyByteBuf) {
        // only on client
        // should note this function is called after EntityJoinLevelEvent
        // this results initEntity -> initCypher order on client side
        // while        initCypher -> initEntity on the server side
        val hasCC = buffer.readBoolean()
        val ccMap = if (hasCC) MOCC_STREAM.decode(buffer) else null
        val steerer = CYPHER_STEERER_STREAM.decode(buffer)
        initCypher(cypher, ccMap, steerer)
        refreshDimensions() // let BB fit effect-radius // server auto handles dimension when creation
    }

    override fun onAddedToLevel() {
        super.onAddedToLevel()
    }

    override fun onRemovedFromLevel() {
//        println("$this about to remove")
        super.onRemovedFromLevel()
    }

    override fun sendPairingData(serverPlayer: ServerPlayer, bundleBuilder: Consumer<CustomPacketPayload>) {
        super.sendPairingData(serverPlayer, bundleBuilder)
    }

    override fun moveOrInterpolateTo(position: Optional<Vec3>, yRot: Optional<Float>, xRot: Optional<Float>) {
        // FIXME entity position flashes at almost always around the 11th tick and the ClientboundMoveEntityPacket / ClientboundEntityPositionSyncPacket is received
//        println("${level().side()} interpolate: ${position()} -> ${position.getOrNull()} ${yRot.isPresent} ${xRot.isPresent} firstTick $firstTick")
        if (level().isClientSide && tickCount < 3) {
//            println("skip this Interpolation")
            return
        }
        super.moveOrInterpolateTo(position, yRot, xRot)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // initialization
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    init {
        noPhysics = true
    }

    abstract override val cypherHolder: Holder<out AbstractProjectileCypher<out AbstractDedicatedCypherProjectile>>

    fun owner() = getOwner()
    override fun getOwner(): Entity? = super<Projectile>.getOwner()
    override fun setOwner(owner: Entity?) = super<Projectile>.setOwner(owner)
    override fun onHit(result: HitResult) {
//        super<Projectile>.onHit(result)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    open fun doEntitySetup() = Unit

    override fun tick() {
        if (firstTick) {
            doEntitySetup()
            debugMsg()
        }

        super.tick()
        doTick()
    }

    protected fun radiusFriendlyParticleCount(base: Int, max: Int = Int.MAX_VALUE): Int =
        base.times(getEffectRadius()).toInt().coerceAtMost(max)
    /** client only */
    protected open fun discardVisualEffect() = Unit


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // assume effectRadius won't change over time
    private var _storedDimensionBacking: EntityDimensions? = null
    override fun getDimensions(pose: Pose): EntityDimensions {
        return _storedDimensionBacking ?:
        type.dimensions.scale(getEffectRadius()).also { _storedDimensionBacking = it }
//            .also { println("side: ${level().isClientSide}"); println(getEffectRadius()); println(it); }
    }

    // dimension cares nothing about practical entity position
    // let position V3 be in the center of AABB instead bottom for convenience
    override fun makeBoundingBox(position: Vec3): AABB {
        if (firstTick) return super.makeBoundingBox(position)
        val wh = getDimensions(pose).width / 2
        return position.centeredAABB(wh)
    }

    override fun isClientAuthoritative(): Boolean {
        // make boomerang client-track smooth
        return super.isClientAuthoritative() ||
                (haveFlag(CypherFlags.MOTION_FOLLOWS_OWNER) && owner()?.isClientAuthoritative == true) ||
                hooksSharedData.homingTarget?.isClientAuthoritative == true
    }
    override fun isLocalClientAuthoritative(): Boolean {
        // make boomerang client-track smooth
        return super.isLocalClientAuthoritative() ||
                (haveFlag(CypherFlags.MOTION_FOLLOWS_OWNER) && owner()?.isLocalClientAuthoritative == true) ||
                hooksSharedData.homingTarget?.isLocalClientAuthoritative == true
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    override fun handleEntityEvent(id: Byte) {
        // trigger on client
        super.handleEntityEvent(id)

        if (level().isClientSide) {
            if (id.toInt() == 3) {
                discardVisualEffect()
            }
        }
    }

    override fun displayFireAnimation() = haveFlag(CypherFlags.WITH_FIRE)

    override fun shouldRender(x: Double, y: Double, z: Double): Boolean = super.shouldRender(x, y, z)
    override fun shouldRenderAtSqrDistance(distance: Double): Boolean {
        val v = getEffectRadius() * getViewScale() * 48
        return distance < v * v
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // miscellaneous
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    override fun getPickResult(): ItemStack? = null // null by default, this is the creative mod middle button pick result
    override fun isPickable() = false // false by default, entirely disable the picking activity // through canBeHitByProjectile, this also prevents cypher-projectile being select by level#getEntities
    override fun canSpawnSprintParticle() = false
    /**
     * since the projectile can't exist without a related cypher,
     * the #deltaMovement initialization will be done automatically, call #shoot is not necessary
     * */
    override fun shoot(x: Double, y: Double, z: Double, velocity: Float, inaccuracy: Float) = Unit // do nothing, don't call
    override fun push(entity: Entity) = Unit
    override fun handlePortal() {
        // TODO
        super.handlePortal()
    }
    // a public method, to get a HitResult by checking if there is any block or entity in the direction of #getViewVector
    // ray cast
//    override fun pick(hitDistance: Double, partialTicks: Float, hitFluids: Boolean): HitResult {
//        return super.pick(hitDistance, partialTicks, hitFluids)
//    }
    override fun hurtServer(level: ServerLevel, source: DamageSource, damage: Float) = false
    override fun hurtClient(source: DamageSource) = false
    override fun ignoreExplosion(explosion: Explosion) = true // this prevents projectile getting kinetic energy from explosion
    override fun isAttackable(): Boolean = false
    override fun skipAttackInteraction(source: Entity): Boolean = true


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private fun debugMsg() {
        CypherNexus.LOGGER.debug("create projectile [{} {}]: [{}]", this, getExisting(), cypher)
        CypherFlags.printFlag(enabledFlags)

        debugAttributes()
    }

    override fun hashCode() = super.hashCode()
    override fun equals(other: Any?) = if (other is Entity) other.id == this.id else false // a kotlin nullable reload
}