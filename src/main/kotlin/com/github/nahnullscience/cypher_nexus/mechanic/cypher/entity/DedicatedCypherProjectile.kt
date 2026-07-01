package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation.CypherEntityBasics
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileNode
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.TriggerType
import com.github.nahnullscience.cypher_nexus.utility.i.IFlagExtension
import com.github.nahnullscience.cypher_nexus.utility.mod.CNCodecs.MOCC_STREAM
import net.minecraft.core.Holder
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntitySpawnReason
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Explosion
import net.minecraft.world.level.Level
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn
import java.util.function.Consumer

abstract class DedicatedCypherProjectile(
    entityType: EntityType<out DedicatedCypherProjectile>,
    level: Level
) : Projectile(entityType, level), IEntityWithComplexSpawn,
    IFlagExtension, ICypherEntity by CypherEntityBasics<DedicatedCypherProjectile>() {
    companion object {
        /** generate projectile with attributes initialized */
        fun <CY> create(
            cypher: AbstractProjectileCypher<*>,
            entityType: EntityType<CY>,
            level: ServerLevel,
            invoker: Entity?,
            direction: Vec3? = null,
            shotState: ShotStateChunk,
            node: ProjectileNode,
        ) : CY where CY : Entity, CY : ICypherEntity {
            val proj = entityType.create(level, EntitySpawnReason.SPAWN_ITEM_USE) ?:
            throw IllegalStateException("Failed to create projectile [$entityType].")
            proj.setOwner(invoker)
            proj.initCypher(cypher, shotState, node)
            proj.initDirection(direction)
            return proj
        }

        fun <T : DedicatedCypherProjectile> createRaw(entityType: EntityType<T>, level: ServerLevel, owner: Entity?) : T {
            val proj = entityType.create(level, EntitySpawnReason.SPAWN_ITEM_USE) ?:
            throw IllegalStateException("Failed to create projectile [$entityType].")
            proj.setOwner(owner)
            return proj
        }
    }
    override fun defineSynchedData(builder: SynchedEntityData.Builder) { }
    override fun onSyncedDataUpdated(key: EntityDataAccessor<*>) {
        super.onSyncedDataUpdated(key)
    }
    override fun readAdditionalSaveData(input: ValueInput) = Unit
    override fun addAdditionalSaveData(output: ValueOutput) = Unit

    override fun writeSpawnData(buffer: RegistryFriendlyByteBuf) {
        // send when entity added to level
        buffer.writeBoolean(ccMap() != null) // write & read relay strictly on order, use a marker to tell client if a map follows
        if (ccMap() != null) {
            MOCC_STREAM.encode(buffer, ccMap()!!)
        }
    }

    override fun readSpawnData(buffer: RegistryFriendlyByteBuf) {
        // only on client
        if (buffer.readBoolean()) {
            val ccMap = MOCC_STREAM.decode(buffer)
            initCypher(cypher, ccMap)
        }
    }

    override fun onAddedToLevel() {
        super.onAddedToLevel()
    }

    override fun sendPairingData(serverPlayer: ServerPlayer, bundleBuilder: Consumer<CustomPacketPayload>) {
        super.sendPairingData(serverPlayer, bundleBuilder)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    abstract override val cypherHolder: Holder<out AbstractProjectileCypher<out DedicatedCypherProjectile>>

    private var _existing: Int? = null
    override fun getExisting(): Int = _existing ?: attributeOrDefault(CypherAttributes.EXISTING).toInt()
    fun setExisting(t: Int) = run { _existing = t }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // initialization
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    override fun tick() {
        // FIXME entity desync "positon-flash" almost always happen at around first time they sync
        // FIXME aiming deviation at high speed
        if (firstTick) debugMsg()
        doTick()
        super.tick() // maybe prune default tick?
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // handle collapse
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    override fun onHit(result: HitResult) {
        super.onHit(result) // distribute hitResult
        hitBoth(result)
        if (level().isClientSide) return
        trigger(TriggerType.COLLISION)

        val canPierce =
            result is BlockHitResult && haveFlag(CypherFlags.IGNORE_BLOCK) ||
                    result is EntityHitResult && haveFlag(CypherFlags.PIERCE_ENTITY)
        if (!canPierce && !canBounce) {
            level().broadcastEntityEvent(this, 3) // combine with #handleEntityEvent
            discardCypher(if (result.type == HitResult.Type.BLOCK) DiscardReason.HIT_BLOCK else DiscardReason.HIT_ENTITY)
        }

    }
    override fun onHitEntity(result: EntityHitResult) {
        super.onHitEntity(result)
        val target = result.entity
        if (notHaveFlag(CypherFlags.SKIP_DAMAGE_CHECK)) {
            val damage = attributeOrDefault(CypherAttributes.DAMAGE)
            if (level() is ServerLevel)
                target.hurtServer(level() as ServerLevel, damageSources().thrown(this, owner()), damage.toFloat())
        }
    }
    override fun onHitBlock(result: BlockHitResult) {
        super.onHitBlock(result)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // trigger & hooks
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /** client only */
    protected open fun discardVisualEffect() = Unit

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    override fun handleEntityEvent(id: Byte) {
        // trigger on client
        super.handleEntityEvent(id)
        if (id.toInt() == 3) {
            discardVisualEffect()
        }
    }

    override fun shouldRender(x: Double, y: Double, z: Double): Boolean = super.shouldRender(x, y, z)
    override fun shouldRenderAtSqrDistance(distance: Double): Boolean = distance < 4096

    override fun displayFireAnimation() = haveFlag(CypherFlags.WITH_FIRE)

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
    fun owner() = getOwner()
    override fun getOwner(): Entity? = super<Projectile>.getOwner()
    override fun setOwner(owner: Entity?) = super<Projectile>.setOwner(owner)

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // miscellaneous
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    override fun getPickResult(): ItemStack? = null // null by default, this is the creative mod middle button pick result
    override fun isPickable() = false // false by default, entirely disable the picking activity
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
    private fun debugMsg() {
        CypherNexus.LOGGER.debug("create projectile {}: {}", this, cypher)
        CypherFlags.printFlag(enabledFlags)

        // modified AttrMap
        attributeMap().forEach { (a, v) ->
            println("$a: $v")
        }
        if (attributeMap().isEmpty()) println("projectile $cypher has no modified attributes")
    }

    override fun hashCode() = super.hashCode()
    override fun equals(other: Any?) = if (other is Entity) other.id == this.id else false // a kotlin nullable reload
}