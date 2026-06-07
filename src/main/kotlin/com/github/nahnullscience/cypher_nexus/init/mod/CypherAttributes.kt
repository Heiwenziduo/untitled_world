package com.github.nahnullscience.cypher_nexus.init.mod

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
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

    // maybe use a chain...
    fun registerAttribute(path: String, default: Double, min: Double = -Double.MAX_VALUE, max: Double = Double.MAX_VALUE,
                          target: CypherAttribute.AttributeApply = CypherAttribute.AttributeApply.PROJECTILE, sync: Boolean = true, hide: Boolean = false)
    : Holder<CypherAttribute> =
        DEFERRED_REGISTER.register(path) { resource ->
            CypherAttribute(
                resource = resource,
                defaultValue = default,
                min = min, max = max,
                sync = sync, applyOn = target, hide = hide
            )
        }


    // ================================ casting process
//    val MANA_DRAIN = registerAttribute("mana_drain")
//    val DRAW = registerAttribute("draw")
//    /** unit is "tick", cast to int at last */
//    val CAST_DELAY = registerAttribute("cast_delay", 0.0, target = CypherAttribute.AttributeApply.INVOKING)
//    /** unit is "tick", cast to int at last */
//    val RECHARGE_TIME = registerAttribute("recharge_time", 0.0, target = CypherAttribute.AttributeApply.INVOKING)
    /** degree */
    val SPREAD = registerAttribute("spread", 0.0, 0.0, 720.0, target = CypherAttribute.AttributeApply.INVOKING)
    val RECOIL = registerAttribute("recoil", 0.0, 0.0, 1000.0, target = CypherAttribute.AttributeApply.INVOKING, hide = true)


    // ================================ projectile
    val DAMAGE = registerAttribute("damage", 0.0, sync = false)
    /** 1.00 <-> 100% */
    val CRIT_CHANCE = registerAttribute("crit_chance", 0.0, 0.0, sync = false)
    val KNOCKBACK = registerAttribute("knockback", 0.0, 0.0, 1000.0, sync = false, hide = true)
    val FORTUNE_LEVEL = registerAttribute("fortune", 0.0, -1.0, 100.0, sync = false, hide = true)
    /** initial speed, in unit block per tick, will show block/sec to player */
    val SPEED = registerAttribute("speed", 0.0, 0.0, 16.0)
    /** tick */
    val EXISTING = registerAttribute("existing", 300.0)
    val EFFECT_RADIUS = registerAttribute("effect_redius", 1.0, 0.0625, 16.0)
    /** int, bounce times */
    val BOUNCE = registerAttribute("bounce", 0.0, 0.0, 100.0)
    /** how much it falls each tick */
    val GRAVITY_FACTOR = registerAttribute("gravity_factor", 0.0, -1.0, 1.0, hide = true)
    /** deltaMovement * (1 - Friction) each tick, 0.01 by default */
    val FRICTION_FACTOR = registerAttribute("friction_factor", 0.01, -9.0, 1.0, hide = true)
}