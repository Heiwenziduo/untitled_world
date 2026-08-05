package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation

import com.github.nahnullscience.cypher_nexus.init.data_driven.ModDamageTypes.CYPHER_DEFAULT
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.steerer.AbstractCypherSteerer
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.steerer.NoSteerer
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookContainer
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HooksSharedData
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk
import com.github.nahnullscience.cypher_nexus.utility.mod.MapOfCypherCounts
import net.minecraft.core.Direction
import net.minecraft.core.registries.Registries
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntitySelector
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.animal.Animal
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult

open class CEContext <CE> : ICEContext<CE> where CE : Entity, CE : ICypherEntity {
    protected lateinit var ce: CE
    protected val level get() = ce.level()
    protected val random get() = ce.random

    override var enabledFlags = CypherFlags.fromFlags() // no flag by default

    override var ccMap: MapOfCypherCounts? = null

    override var hooks: HookContainer? = null
    override val hooksSharedData = HooksSharedData<CE>()

    override var steerer: AbstractCypherSteerer = NoSteerer

    override var hue: Int? = null
    override var hueFloatArray: FloatArray? = null

    private var ownerD: Entity? = null

    override fun initCypher(
        cypher: AbstractProjectileCypher<*>,
        shotState: ShotStateChunk?,
        steerer: AbstractCypherSteerer?
    ) {
        enabledFlags = (shotState?.enabledFlags ?: 0) or cypher.flags
        hooks = shotState?.hooks
        ccMap = shotState?.ccMap
        hue = shotState?.dyeAccumulator?.color
        hueFloatArray = shotState?.dyeAccumulator?.colorArray
        steerer?.let { this.steerer = it }
    }

    override fun initEntity(ce: CE) = let { this@CEContext.ce = ce }

    override fun getOwner(): Entity? = ownerD
    override fun setOwner(owner: Entity?) = let { ownerD = owner }


    override fun getDamageSource(): DamageSource {
        return DamageSource(
            level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(CYPHER_DEFAULT),
            ce,
            ce.owner,
            ce.position()
        )
    }


    override fun canHitTarget(target: Entity): Boolean {
        if (!target.canBeHitByProjectile()) {
            return false // vanilla logic, for item-entities
        }
        ce.owner?.let { owner ->
            if (!ce.canHurtOwner(ce) &&
                (owner == target || owner.isPassengerOfSameVehicle(target))) return false
        }
        return true
    }
    override fun whenHit(result: HitResult, direction: Direction) = Unit
    override fun whenHitEntity(result: EntityHitResult, direction: Direction) = Unit
    override fun whenHitBlock(result: BlockHitResult, direction: Direction) = Unit


    override fun canHomeTarget(target: Entity): Boolean {
        return ce.canHitTarget(target)
                && target is LivingEntity
                && target !is Animal
                && target !is ICypherEntity
                && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(target)
                && !target.isInvisible
                && !target.isInvulnerable
                && target.isAlive
                && target != ce.owner
                && !target.`is`(EntityType.ARMOR_STAND)
    }

    override fun whileHomeTarget(target: Entity) {}
}