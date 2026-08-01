package com.github.nahnullscience.cypher_nexus.init

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.content.entity.*
import com.github.nahnullscience.cypher_nexus.content.entity.statics.SummonedExplosion
import com.github.nahnullscience.cypher_nexus.content.entity.statics.SummonedLightning
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractDedicatedCypherProjectile
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS

object ModEntities {
    val DEFERRED_REGISTER: DeferredRegister.Entities = DeferredRegister.createEntities(CypherNexus.MOD_ID)

    fun register() {
        DEFERRED_REGISTER.register(MOD_BUS)
    }

    /**
     * check [EntityType]
     * */
    fun <T : AbstractDedicatedCypherProjectile> registerDedicated(
        name: String,
        factory: EntityType.EntityFactory<T>,
        category: MobCategory = MobCategory.MISC,
        updateInterval: Int = 20
    ): DeferredHolder<EntityType<*>, EntityType<T>> {
        return DEFERRED_REGISTER.registerEntityType(name, factory, category) { builder ->
            builder
                .sized(0.125f, 0.125f)
                .eyeHeight(0.0625f)
                .noLootTable()
                .fireImmune()
                // Prevents the entity from being saved to disk.
                .noSave()
                // Disables the entity being summonable via /summon.
                .noSummon()
                // The range in which the entity is kept loaded by the client, capped at client's chunk view distance
                .clientTrackingRange(10)
                // How often update packets are sent for this entity, in once every x ticks. This is set to higher values
                // for entities that have predictable movement patterns, for example, projectiles. Defaults to 3.
                .updateInterval(updateInterval)
        }
    }


    // projectile ////////////////////////////////////////////////////////////////////////////////////////
    // using "cypher_" prefix is a convention
    val CYPHER_ARROW = registerDedicated("cypher_arrow", ::Arrow)
    val CYPHER_SNOWBALL = registerDedicated("cypher_snowball", ::Snowball)
    val CYPHER_ENDER_TELEPORTATION = registerDedicated("cypher_ender_teleportation", ::EnderTeleportation)
    val CYPHER_ENDER_RECALL = registerDedicated("cypher_ender_recall", ::EnderRecall)
    val CYPHER_SPAWN_EGG = registerDedicated("cypher_spawn_egg", ::SpawnEgg)
    val CYPHER_BUBBLE_COLUMN = registerDedicated("cypher_bubble_column", ::BubbleColumn)
    val CYPHER_LLAMA_SPIT = registerDedicated("cypher_llama_spit", ::LlamaSpit)
    val CYPHER_DRILLING_BOLT = registerDedicated("cypher_drilling_bolt", ::DrillingBolt)
    val CYPHER_DRILLING_BLAST = registerDedicated("cypher_drilling_blast", ::DrillingBlast)
    val CYPHER_SMOKE_BOMB = registerDedicated("cypher_smoke_bomb", ::SmokeBomb)

    // static-projectile //////////////////////////////////////////////////////////////////////////////////
    val CYPHER_EXPLOSION = registerDedicated("cypher_explosion", ::SummonedExplosion)
    val CYPHER_LIGHTING = registerDedicated("cypher_lighting", ::SummonedLightning)
}

