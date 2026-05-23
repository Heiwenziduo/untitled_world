package com.github.nahnullscience.cypher_nexus.init.mod

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.content.cypher.modifier.*
import com.github.nahnullscience.cypher_nexus.content.cypher.other.AddTrigger
import com.github.nahnullscience.cypher_nexus.content.cypher.projectile.*
import com.github.nahnullscience.cypher_nexus.content.cypher.static_projectile.ExplosionCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.EmptyCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttributeOperation
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import com.github.nahnullscience.cypher_nexus.mechanic.exception.CypherNotFoundException
import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.RegistryBuilder
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS

/**
 *
 * */
object ModCyphers {
    val RESOURCE_KEY: ResourceKey<Registry<AbstractCypher>> =
        ResourceKey.createRegistryKey(CypherNexus.modResource("cypher"))
    val REGISTRY: Registry<AbstractCypher> = RegistryBuilder(RESOURCE_KEY).sync(true).defaultKey(EmptyCypher.resource).create()

    val DEFERRED_REGISTER: DeferredRegister<AbstractCypher> =
        DeferredRegister.create(REGISTRY, CypherNexus.MOD_ID)

    fun register() {
        DEFERRED_REGISTER.register(MOD_BUS)
    }

    fun registerCypher(cypher: AbstractCypher): Holder<AbstractCypher> {
        return DEFERRED_REGISTER.register(cypher.resource.path) { -> cypher }
    }

    fun getCypher(resource: ResourceLocation): AbstractCypher? = REGISTRY.get(resource)
    fun getCypherOrThrow(resource: ResourceLocation): AbstractCypher {
        val c = REGISTRY.get(resource)
        if (c == null) throw CypherNotFoundException("missing cypher: ${resource.namespace}-${resource.path}")
        return c
    }


    // technical
    val EMPTY_CYPHER = registerCypher(EmptyCypher)

    // ==========================================================================================
    // # will present in register order #
    // ==========================================================================================

    // projectile
    val ARROW = registerCypher(ArrowCypher)
    val SNOWBALL = registerCypher(SnowballCypher)
    val ENDER_TELEPORTATION = registerCypher(EnderTeleportationCypher)
    val ENDER_RECALL = registerCypher(EnderRecallCypher)
    val LLAMA_SPIT = registerCypher(LlamaSpitCypher)
    val SPAWN_EGG = registerCypher(SpawnEggCypher)

    // static projectile
    val EXPLOSION = registerCypher(ExplosionCypher)

    // modifier
    val POWER = registerCypher(
        SimpleModifier(5f, "power")
        .attribute(CypherAttributes.DAMAGE, CypherAttributeOperation.ADD, 1.0)
        .attribute(CypherAttributes.RECOIL, CypherAttributeOperation.ADD, 1.0))
    val BRISK = registerCypher(SimpleModifier(5f, "brisk").attribute(CypherAttributes.SPEED, CypherAttributeOperation.MULTIPLY_TOTAL, 2.0))
    val FIERY = registerCypher(FieryCypher)
    val ANTIGRAVITY = registerCypher(SimpleModifier(1f, "antigravity").attribute(CypherAttributes.GRAVITY_FACTOR, CypherAttributeOperation.MULTIPLY_TOTAL, -1.0))




//    val FOCUS = registerCypher(SimpleModifier(1f, "focus").attribute(CypherAttributes.SPREAD, CypherAttributeOperation.ADD, -30.0))
////    val FIERCE = registerCypher(SimpleModifier(20f, "fierce").attribute(CypherAttributes.CRIT_CHANCE, CypherAttributeOperation.ADD, 0.15))
//    val POWER_IMBUE = registerCypher(SimpleModifier(66f, "power_imbue")
//        .attribute(CypherAttributes.DAMAGE, CypherAttributeOperation.MULTIPLY_BASE, 0.25)
//        .attribute(CypherAttributes.SPEED, CypherAttributeOperation.MULTIPLY_BASE, 0.25)
//        .attribute(CypherAttributes.RECOIL, CypherAttributeOperation.ADD, 1.0)
//        .attribute(CypherAttributes.CAST_DELAY, CypherAttributeOperation.ADD, 8.0))
//    val RECOIL_LESS = registerCypher(SimpleModifier(1f, "recoil_less").attribute(CypherAttributes.RECOIL, CypherAttributeOperation.ADD, -5.0))
//    val RECOIL_MORE = registerCypher(SimpleModifier(1f, "recoil_more").attribute(CypherAttributes.RECOIL, CypherAttributeOperation.ADD, 5.0))
//    val RELOAD = registerCypher(SimpleModifier(12f, "reload")
//        .attribute(CypherAttributes.CAST_DELAY, CypherAttributeOperation.ADD, -4.0)
//        .attribute(CypherAttributes.RECHARGE_TIME, CypherAttributeOperation.ADD, -6.0))




    val MANA_SURGE = registerCypher(
        SimpleModifier(-50f, "mana_surge")
        .attribute(CypherAttributes.CAST_DELAY, CypherAttributeOperation.ADD, 4.0)
        .attribute(CypherAttributes.RECHARGE_TIME, CypherAttributeOperation.ADD, 4.0))
    val HEAVILY_STRONG = registerCypher(
        SimpleModifier(20f, "heavily_strong")
        .attribute(CypherAttributes.DAMAGE, CypherAttributeOperation.ADD, 4.0)
        .attribute(CypherAttributes.SPEED, CypherAttributeOperation.MULTIPLY_TOTAL, 0.75)
        .attribute(CypherAttributes.RECOIL, CypherAttributeOperation.ADD, 2.0)
        .attribute(CypherAttributes.GRAVITY_FACTOR, CypherAttributeOperation.ADD, 0.01)
        .attribute(CypherAttributes.FRICTION_FACTOR, CypherAttributeOperation.MULTIPLY_BASE, 1.0))
    val PEACEFUL = registerCypher(SimpleModifier(5f, "peaceful").flag(CypherFlags.NO_DAMAGE))
    val BOUNCY = registerCypher(
        SimpleModifier(
            5f,
            "bouncy"
        ).attribute(CypherAttributes.BOUNCE, CypherAttributeOperation.ADD, 5.0))
    val NO_MORE_BOUNCE = registerCypher(SimpleModifier(0f, "no_more_bounce").attribute(CypherAttributes.BOUNCE, CypherAttributeOperation.SET_ALL, 0.0))
    val NO_MORE_DAMAGE = registerCypher(SimpleModifier(0f, "no_more_damage").attribute(CypherAttributes.DAMAGE, CypherAttributeOperation.SET_ALL, 0.0))
    val EXTEND_TIME = registerCypher(
        SimpleModifier(35f, "extend_time")
        .attribute(CypherAttributes.RECHARGE_TIME, CypherAttributeOperation.ADD, 4.0)
        .attribute(CypherAttributes.EXISTING, CypherAttributeOperation.MULTIPLY_TOTAL, 1.5))
    val CURTAIL_TIME = registerCypher(
        SimpleModifier(35f, "curtail_time")
        .attribute(CypherAttributes.RECHARGE_TIME, CypherAttributeOperation.ADD, -4.0)
        .attribute(CypherAttributes.EXISTING, CypherAttributeOperation.MULTIPLY_TOTAL, 0.66))

    val HOMING = registerCypher(HomingCypher)
    val PIERCE_ENTITY = registerCypher(PierceEntityCypher)
    val DAEDALUS = registerCypher(DaedalusCypher)
    val NULLIFIER = registerCypher(
        SimpleModifier(44f, "nullifier")
        .attribute(CypherAttributes.EXISTING, CypherAttributeOperation.SET_ALL, 1.0)
        .attribute(CypherAttributes.CAST_DELAY, CypherAttributeOperation.ADD, -7.0)
        .attribute(CypherAttributes.RECHARGE_TIME, CypherAttributeOperation.ADD, -5.0))

    const val COLOR_MULTI_INVOKE = 0xFF98A087.toInt()
    val DOUBLE_INVOKING = registerCypher(SimpleModifier(1f, "double_invoking", 2, COLOR_MULTI_INVOKE))
    val TREBLE_INVOKING = registerCypher(SimpleModifier(5f, "treble_invoking", 3, COLOR_MULTI_INVOKE))
    val QUADRUPLE_INVOKING = registerCypher(SimpleModifier(20f, "quadruple_invoking", 4, COLOR_MULTI_INVOKE))
    val OCTUPLE_INVOKING = registerCypher(SimpleModifier(50f, "octuple_invoking", 8, COLOR_MULTI_INVOKE))
    val DOUBLE_SCATTER = registerCypher(
        SimpleModifier(0f, "double_scatter", 2, COLOR_MULTI_INVOKE)
        .attribute(CypherAttributes.SPREAD, CypherAttributeOperation.ADD, 20.0))
    val TREBLE_SCATTER = registerCypher(
        SimpleModifier(1f, "treble_scatter", 3, COLOR_MULTI_INVOKE)
        .attribute(CypherAttributes.SPREAD, CypherAttributeOperation.ADD, 30.0))
    val QUADRUPLE_SCATTER = registerCypher(
        SimpleModifier(5f, "quadruple_scatter", 4, COLOR_MULTI_INVOKE)
        .attribute(CypherAttributes.SPREAD, CypherAttributeOperation.ADD, 40.0))

    // passive

    // other
    val ADD_TRIGGER = registerCypher(AddTrigger)
}