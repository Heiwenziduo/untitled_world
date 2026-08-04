package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation

import com.github.nahnullscience.cypher_nexus.init.data_driven.ModDamageTypes.CYPHER_DEFAULT
import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.DiscardReason
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity.Companion.LOW_SPEED_THRESHOLD_SQR
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.steerer.AbstractCypherSteerer
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.steerer.NoSteerer
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookContainer
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HooksSharedData
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk
import com.github.nahnullscience.cypher_nexus.utility.mod.MapOfCypherCounts
import net.minecraft.core.registries.Registries
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity

open class CEContext <CE> : ICEContext<CE> where CE : Entity, CE : ICypherEntity {
    protected lateinit var cyEntity: CE
    protected val level get() = cyEntity.level()
    protected val random get() = cyEntity.random

    override var enabledFlags = CypherFlags.fromFlags() // no flag by default

    override var ccMap: MapOfCypherCounts? = null

    override var hooks: HookContainer? = null
    override val hooksSharedData = HooksSharedData<CE>()

    override var steerer: AbstractCypherSteerer = NoSteerer

    override var hue: Int? = null
    override var hueFloatArray: FloatArray? = null

    private var owner: Entity? = null

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

    override fun initEntity(ce: CE) = let { cyEntity = ce }

    override fun getOwner(): Entity? = owner
    override fun setOwner(owner: Entity?) = let { this.owner = owner }

    override fun getExisting(): Int = cyEntity.getAttributeOrDefault(CypherAttributes.EXISTING).toInt()
    override fun getBounce(): Int = cyEntity.getAttributeOrDefault(CypherAttributes.BOUNCE).toInt()
    override fun getGravityFactor(): Double = cyEntity.getAttributeOrDefault(CypherAttributes.GRAVITY_FACTOR)
    override fun getSpeedFactor(): Double = 1f - cyEntity.getAttributeOrDefault(CypherAttributes.FRICTION_FACTOR)
    override fun getEffectRadius(): Float = cyEntity.getAttributeOrDefault(CypherAttributes.EFFECT_RADIUS).toFloat()

    override fun getDamageSource(): DamageSource {
        return DamageSource(
            level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(CYPHER_DEFAULT),
            cyEntity,
            cyEntity.owner,
            cyEntity.position()
        )
    }
}