package com.github.nahnullscience.cypher_nexus.init

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.content.entity.AbstractCypherProjectile
import net.minecraft.core.registries.Registries
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import net.neoforged.neoforge.registries.DeferredRegister
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS
import java.util.function.Supplier

object ModEntities {
    val DEFERRED_REGISTER: DeferredRegister<EntityType<*>> =
        DeferredRegister.create(Registries.ENTITY_TYPE, CypherNexus.MOD_ID)

    fun register() {
        DEFERRED_REGISTER.register(MOD_BUS)
    }

    val CYPHER_PROJECTILE: Supplier<EntityType<AbstractCypherProjectile>> =
        DEFERRED_REGISTER.register("cypher_projectile") { resource ->
            EntityType.Builder.of({ t, l -> AbstractCypherProjectile(t, l) }, MobCategory.MISC)
                .cypherBasic()
                .build(resource.path)
        }
}

private fun EntityType.Builder<AbstractCypherProjectile>.cypherBasic(updateInterval: Int = 10): EntityType.Builder<AbstractCypherProjectile> {
    sized(0.125f, 0.125f)
    // Prevents the entity from being saved to disk.
    noSave()
    // Disables the entity being summonable via /summon.
    noSummon()
    // The range in which the entity is kept loaded by the client, capped at client's chunk view distance
    clientTrackingRange(10)
    // How often update packets are sent for this entity, in once every x ticks. This is set to higher values
    // for entities that have predictable movement patterns, for example, projectiles. Defaults to 3.
    updateInterval(updateInterval)
    return this
}
