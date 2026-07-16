package com.github.nahnullscience.cypher_nexus.init.mod

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute.AttributeApply
import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.RegistryBuilder
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS

/**
 * registry attribute keys
 * */
object CypherAttributes {
    val RESOURCE_KEY: ResourceKey<Registry<CypherAttribute>> =
        ResourceKey.createRegistryKey(CypherNexus.modResource("cypher/attribute"))
    val REGISTRY: Registry<CypherAttribute> = RegistryBuilder(RESOURCE_KEY).sync(true).create()

    val DEFERRED_REGISTER: DeferredRegister<CypherAttribute> =
        DeferredRegister.create(REGISTRY, CypherNexus.MOD_ID)

    fun register() {
        DEFERRED_REGISTER.register(MOD_BUS)
    }

    fun registerAttribute(path: String, factory: (CypherAttribute.Builder) -> CypherAttribute.Builder) : Holder<CypherAttribute> =
        DEFERRED_REGISTER.register(path) { resource -> factory(CypherAttribute.Builder(resource)).build() }


    // ================================ invoking
    /** degree */
    val SPREAD = registerAttribute("spread")
    { builder -> builder.max(720.0).applyOn(AttributeApply.INVOKING) }
    val RECOIL = registerAttribute("recoil")
    { builder -> builder.max(1000.0).applyOn(AttributeApply.INVOKING).hide() }
    // ================================ projectile
    val DAMAGE = registerAttribute("damage")
    { builder -> builder.min(-Double.MAX_VALUE).noSync() }
    /** 1.00 <-> 100% */
    val CRIT_CHANCE = registerAttribute("crit_chance")
    { builder -> builder.noSync() }
    val KNOCKBACK = registerAttribute("knockback")
    { builder -> builder.max(1000.0).noSync().hide() }
    val FORTUNE_LEVEL = registerAttribute("fortune")
    { builder -> builder.min(-1.0).max(32.0).noSync().hide() }
    /** initial speed, in unit block per tick, will show block/sec to player */
    val SPEED = registerAttribute("speed")
    { builder -> builder.max(8.0) }
    /** tick, 200 by default */
    val EXISTING = registerAttribute("existing")
    { builder -> builder.default(200.0) }
    /** default 1.0 */
    val EFFECT_RADIUS = registerAttribute("effect_radius")
    { builder -> builder.default(1.0).min(0.25).max(16.0) }
    /** int, bounce times */
    val BOUNCE = registerAttribute("bounce")
    { builder -> builder.max(127.0) }
    /** how much it falls each tick, 0.0 by default */
    val GRAVITY_FACTOR = registerAttribute("gravity_factor")
    { builder -> builder.min(-1.0).max(1.0).hide() }
    /** deltaMovement * (1 - Friction) each tick, 0.03 by default */
    val FRICTION_FACTOR = registerAttribute("friction_factor")
    { builder -> builder.default(0.03).min(-9.0).max(1.0).hide() }
}