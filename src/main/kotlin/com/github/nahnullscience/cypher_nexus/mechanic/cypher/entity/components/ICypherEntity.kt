package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractDedicatedCypherProjectile
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntityAttributeAccessor.Companion.getAttributeOrDefault
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation.CypherEntityDelegation
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.steerer.AbstractCypherSteerer
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileNode
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotState
import com.github.nahnullscience.cypher_nexus.utility.mod.MapOfCypherCounts
import net.minecraft.core.Holder
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.Vec3
import net.neoforged.bus.api.EventPriority
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent

/**
 * define data pieces that all cypher-entity would require.
 * those pieces then could be delivered through [CypherEntityDelegation].
 * */
interface ICypherEntity :
    ICypherEntityLogicContext,
    ICypherEntityPhysics
{
    val cypherHolder: Holder<out AbstractProjectileCypher<*>>

    /**
     * initialize from [MapOfCypherCounts]
     * */
    fun initCypher(
        cypher: AbstractProjectileCypher<*>,
        ccMap: MapOfCypherCounts?,
        steerer: AbstractCypherSteerer
    )

    /**
     * init from [ShotState]
     * */
    fun initCypher(
        cypher: AbstractProjectileCypher<*>,
        shotState: ShotState,
        node: ProjectileNode?,
        steerer: AbstractCypherSteerer?
    )

    /**
     *
     * */
    fun <CE> initEntity(ce: CE) where CE : Entity, CE : ICypherEntity

    /**
     *
     * */
    fun initPositionDirection(position: Vec3, direction: Vec3)


    fun getDirectionInitial(): Vec3
    fun getPositionInitial(): Vec3

    fun printDebugMsg(o: Any? = null) {}


    ///////////////////////////// helpers /////////////////////////////////
    @EventBusSubscriber(modid = CypherNexus.MOD_ID)
    companion object {
        @SubscribeEvent(priority = EventPriority.NORMAL)
        private fun initCypherEntity(event: EntityJoinLevelEvent) {
            val entity = event.entity
            if (entity is AbstractDedicatedCypherProjectile || entity is ICypherEntity) {
                entity.initEntity(entity)
            }
        }

        const val CLIP_MARGIN = 0.2f
        const val HIT_BB_INFLATION = 0.25

        const val MAX_BOUNCE_PER_TICK = 16

        const val GENERIC_CAPTURE_RADIUS = 8.0
        const val GENERIC_CAPTURE_RADIUS_SQR = GENERIC_CAPTURE_RADIUS * GENERIC_CAPTURE_RADIUS

        const val LOW_SPEED_THRESHOLD = 0.03
        const val LOW_SPEED_THRESHOLD_SQR = LOW_SPEED_THRESHOLD * LOW_SPEED_THRESHOLD

        const val KINETIC_DAMAGE_SPEED = 0.25
        const val KINETIC_DAMAGE_SPEED_SQR = KINETIC_DAMAGE_SPEED * KINETIC_DAMAGE_SPEED

        inline val ICypherEntity.cypher get() = cypherHolder.value()
        fun ICypherEntity.canNotHurtOwner(): Boolean = !canHurtOwner()

        inline val ICypherEntity.collideWithBlocks get() =
            noFlagsNone(CypherFlags.IGNORE_BLOCK, CypherFlags.PENETRATE_WORLD)

        inline val ICypherEntity.collideWithEntities get() =
            noFlagsNone(CypherFlags.PENETRATE_WORLD)

        fun ICypherEntity.exertDamage(level: ServerLevel, target: Entity) {
            var damage = this@exertDamage.getAttributeOrDefault(CypherAttributes.DAMAGE)
            var crit = this@exertDamage.getAttributeOrDefault(CypherAttributes.CRIT_CHANCE)
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
}
