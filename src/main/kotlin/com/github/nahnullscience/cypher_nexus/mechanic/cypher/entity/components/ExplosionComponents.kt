package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity.Companion.canNotHurtOwner
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntityAttributeAccessor.Companion.getDamage
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntityAttributeAccessor.Companion.getEffectRadius
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import com.github.nahnullscience.cypher_nexus.utility.isOwnerOf
import net.minecraft.core.Holder
import net.minecraft.core.particles.ExplosionParticleInfo
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.util.random.WeightedList
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Explosion
import net.minecraft.world.level.ExplosionDamageCalculator
import net.minecraft.world.level.Level.ExplosionInteraction
import kotlin.math.sqrt

open class ExplosionSettings <CE> (
    val ce: CE,
    var radiusSqr: Float = 4f,
    var damageFactor: Float = 4f,
    var smallParticle: ParticleOptions = ParticleTypes.EXPLOSION,
    var largeParticle: ParticleOptions = ParticleTypes.EXPLOSION_EMITTER,
    var sound: Holder<SoundEvent> = SoundEvents.GENERIC_EXPLODE,
    var blockParticles: WeightedList<ExplosionParticleInfo>? = null,
    private val interaction: ExplosionInteraction? = null
) : ExplosionDamageCalculator() where CE : Entity, CE : ICypherEntity {

    val source get() = ce.owner // delay getter to avoid init problems

    open val blockInteract: ExplosionInteraction get() = interaction ?:
    if (ce.hasFlag(CypherFlags.SAFE_EXPLODE)) ExplosionInteraction.NONE else ExplosionInteraction.BLOCK

    open val damageSource get() = ce.getExplosionDamageSource()

    open val fire get() = ce.hasFlag(CypherFlags.WITH_FIRE)

    fun getEffectRadius(): Float = sqrt(radiusSqr * ce.getEffectRadius())

    open fun effectRadiusSqr(): Float = radiusSqr * ce.getEffectRadius()

    open fun getDamage(): Float = damageFactor * ce.getDamage()

    override fun shouldDamageEntity(explosion: Explosion, entity: Entity): Boolean {
        if (ce.hasFlag(CypherFlags.SKIP_DAMAGE_CHECK)) return false
        if (ce.canNotHurtOwner() && entity.isOwnerOf(ce)) return false
        return super.shouldDamageEntity(explosion, entity)
    }

    override fun getEntityDamageAmount(explosion: Explosion, entity: Entity, exposure: Float): Float {
//        if (entity is LivingEntity) {
//            entity.invulnerableTime
//        }
        val base = getDamage()
        val dis = entity.position().vectorTo(explosion.center()).lengthSqr().toFloat()
        val rad = effectRadiusSqr()
        return (exposure * exposure * (1f - dis / rad) * base).coerceAtLeast(0.5f) // inverse proportion to the square of distance
    }

    open fun explode(level: ServerLevel, x: Double, y: Double, z: Double, factor: Float = 1f) {
        /**
         * @see net.minecraft.world.level.ServerExplosion
         * */
        level.explode(
            source,
            damageSource,
            this,
            x, y, z,
            getEffectRadius() * factor,
            fire,
            blockInteract,
            smallParticle,
            largeParticle,
            blockParticles ?: defaultExplodeBlockParticles,
            sound
        )
    }

    override fun toString(): String {
        return "[explosion setting] rad: $radiusSqr; dama: $damageFactor; small: $smallParticle; large: $largeParticle; sound: $sound; interact: $blockInteract;"
    }

    companion object {
        val defaultExplodeBlockParticles = WeightedList.builder<ExplosionParticleInfo>()
            .add(ExplosionParticleInfo(ParticleTypes.POOF, 0.5f, 1.0f))
            .add(ExplosionParticleInfo(ParticleTypes.SMOKE, 1.0f, 1.0f))
            .build();
    }
}