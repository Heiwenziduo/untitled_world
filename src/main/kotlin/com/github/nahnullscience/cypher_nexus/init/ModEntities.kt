package com.github.nahnullscience.cypher_nexus.init

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.content.entity.Arrow
import com.github.nahnullscience.cypher_nexus.content.entity.EnderRecall
import com.github.nahnullscience.cypher_nexus.content.entity.EnderTeleportation
import com.github.nahnullscience.cypher_nexus.content.entity.LlamaSpit
import com.github.nahnullscience.cypher_nexus.content.entity.Snowball
import com.github.nahnullscience.cypher_nexus.content.entity.SpawnEgg
import com.github.nahnullscience.cypher_nexus.content.entity.statics.SummonExplosion
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractCypherProjectile
import com.mojang.datafixers.types.templates.Sum
import net.minecraft.core.registries.Registries
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import net.neoforged.neoforge.registries.DeferredRegister
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS
import java.util.function.Supplier

object ModEntities {
    val DEFERRED_REGISTER: DeferredRegister.Entities = DeferredRegister.createEntities(CypherNexus.MOD_ID)

    fun register() {
        DEFERRED_REGISTER.register(MOD_BUS)
    }

    fun <T : AbstractCypherProjectile> register(
        name: String,
        factory: EntityType.EntityFactory<T>,
        category: MobCategory = MobCategory.MISC,
        updateInterval: Int = 10
    ): Supplier<EntityType<T>> {
        return DEFERRED_REGISTER.registerEntityType(name, factory, category) { builder -> builder.cypherBasic(updateInterval) }
    }

    val CYPHER_ARROW = register("cypher_arrow", ::Arrow)
    val CYPHER_SNOWBALL = register("cypher_snowball", ::Snowball)
    val CYPHER_ENDER_TELEPORTATION = register("cypher_ender_teleportation", ::EnderTeleportation)
    val CYPHER_ENDER_RECALL = register("cypher_ender_recall", ::EnderRecall)
    val CYPHER_SPAWN_EGG = register("cypher_spawn_egg", ::SpawnEgg)
    val CYPHER_LLAMA_SPIT = register("cypher_llama_spit", ::LlamaSpit)

    val CYPHER_EXPLOSION = register("cypher_explosion", ::SummonExplosion)
}

private fun <T : AbstractCypherProjectile> EntityType.Builder<T>.cypherBasic(updateInterval: Int)
: EntityType.Builder<T> {
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
