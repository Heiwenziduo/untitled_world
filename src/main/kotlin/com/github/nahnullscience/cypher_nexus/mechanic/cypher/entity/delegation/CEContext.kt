package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation

import com.github.nahnullscience.cypher_nexus.init.data_driven.ModDamageTypes.CYPHER_DEFAULT
import com.github.nahnullscience.cypher_nexus.init.data_driven.ModDamageTypes.CYPHER_DEFAULT_EXPLOSION
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.AttributeFastMap
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ExplosionSettings
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity.Companion.canNotHurtOwner
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity.Companion.cypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.steerer.AbstractCypherSteerer
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.steerer.NoSteerer
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookContainer
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HooksSharedData
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotState
import com.github.nahnullscience.cypher_nexus.utility.isOwnerOf
import com.github.nahnullscience.cypher_nexus.utility.mod.MapOfCypherCounts
import net.minecraft.core.registries.Registries
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntitySelector
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.animal.Animal

open class CEContext <CE> : ICEContext<CE> where CE : Entity, CE : ICypherEntity {
    protected lateinit var ce: CE
    protected val level get() = ce.level()
    protected val random get() = ce.random

    protected val attributeMap = AttributeFastMap()
    override var enabledFlags = 0 // no flag by default

    override var ccMap: MapOfCypherCounts? = null

    override var hooks: HookContainer? = null
    override val hooksSharedData = HooksSharedData()

    override var steerer: AbstractCypherSteerer = NoSteerer

    override var dyed: Boolean = false
    override var hue: Int = 0
    override lateinit var hueFloatArray: FloatArray

    private var ownerD: Entity? = null

    override fun initCypher(
        cypher: AbstractProjectileCypher<*>,
        shotState: ShotState?,
        steerer: AbstractCypherSteerer?
    ) {
        enabledFlags = (shotState?.enabledFlags ?: 0) or cypher.flags
        hooks = shotState?.hooks
        ccMap = shotState?.ccMap

        shotState?.computeAttribute(attributeMap, cypher)
        shotState?.dyeAccumulator?.let {
            if (it.isResolved) {
                dyed = true
                hue = it.color
                hueFloatArray = it.colorArray
            }
        } ?: run { hueFloatArray = floatArrayOf() }
        steerer?.let { this.steerer = it }
    }

    override fun initEntity(ce: CE) { this@CEContext.ce = ce }

    override fun getOwner(): Entity? = ownerD
    override fun setOwner(owner: Entity?) = let { ownerD = owner }
    override fun canHurtOwner(): Boolean  = ce.hasFlag(CypherFlags.HURT_OWNER) && ce.tickCount > 1

    override fun initExplosion(): ExplosionSettings<*>? {
        return if (ce.hasFlag(CypherFlags.EXPLOSIVE)) ExplosionSettings(ce)
        else null
    }
//    override fun canHitMultipleTarget(): Boolean =
//        ce.hasFlagsAny(CypherFlags.PHYSICS_SOLID, CypherFlags.PIERCE_ENTITY) || ce.getBounce() > 0


    override fun getDamageSource(): DamageSource {
        return DamageSource(
            level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(CYPHER_DEFAULT),
            ce,
            ce.owner,
            ce.position()
        )
    }

    override fun getExplosionDamageSource(): DamageSource {
        return DamageSource(
            level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(CYPHER_DEFAULT_EXPLOSION),
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
            if (ce.canNotHurtOwner() && (target.isOwnerOf(ce) || owner.isPassengerOfSameVehicle(target)))
                return false
        }
        return true
    }


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

    override fun hasModifiedAttribute(): Boolean {
        return attributeMap.isNotEmpty()
    }

    override fun hasModifiedAttribute(attr: CypherAttribute): Boolean {
        return attributeMap.hasAttribute(attr)
    }

    override fun getAttributeOrDefault(attr: CypherAttribute): Double {
        return attributeMap.getDouble(attr).let {
            if (it.isNaN()) ce.cypher.getAttrOrDefault(attr)
            else it
        }
    }

    override fun setAttribute(
        attr: CypherAttribute,
        value: Double
    ): Double {
        return attributeMap.setAttribute(attr, value).let { if (it.isNaN()) attr.defaultValue else it }
    }

    override fun printDebugMsg(o: Any?) {
        println("Attributes: ")
        if (hasModifiedAttribute())
        for ((a, d) in attributeMap) {
            println("$a: $d")
        }
        else println("no modified attribute")
    }
}
