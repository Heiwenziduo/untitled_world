package com.github.nahnullscience.cypher_nexus.init.mod

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.content.cypher.modifier.*
import com.github.nahnullscience.cypher_nexus.content.cypher.other.AbstractAddTrigger
import com.github.nahnullscience.cypher_nexus.content.cypher.other.AbstractRequirement
import com.github.nahnullscience.cypher_nexus.content.cypher.other.RequirementLowHP
import com.github.nahnullscience.cypher_nexus.content.cypher.other.RequirementNotPlayer
import com.github.nahnullscience.cypher_nexus.content.cypher.projectile.*
import com.github.nahnullscience.cypher_nexus.content.cypher.static_projectile.ExplosionCypher
import com.github.nahnullscience.cypher_nexus.content.cypher.utility.InnerForceCypher
import com.github.nahnullscience.cypher_nexus.content.cypher.utility.RefresherRingCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.EmptyCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttributeOperation
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.TriggerType
import com.github.nahnullscience.cypher_nexus.utility.exception.CypherNotFoundException
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
object Cyphers {
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

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // projectile
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val ARROW = registerCypher(ArrowCypher)
    val SNOWBALL = registerCypher(SnowballCypher)
    val ENDER_TELEPORTATION = registerCypher(EnderTeleportationCypher)
    val ENDER_RECALL = registerCypher(EnderRecallCypher)
    val LLAMA_SPIT = registerCypher(LlamaSpitCypher)
    val SPAWN_EGG = registerCypher(SpawnEggCypher)

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // static projectile
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val EXPLOSION = registerCypher(ExplosionCypher)

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // modifier
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val POWER = registerCypher(
        SimpleModifier("power", 5f)
            .delay(1)
            .attribute(CypherAttributes.DAMAGE, CypherAttributeOperation.ADD, 1.0)
            .attribute(CypherAttributes.RECOIL, CypherAttributeOperation.ADD, 1.0)
            .modifier())
    val BRISK = registerCypher(
        SimpleModifier("brisk", 5f)
            .delay(-1)
            .attribute(CypherAttributes.SPEED, CypherAttributeOperation.MULTIPLY_TOTAL, 2.0)
            .modifier())
    val FIERY = registerCypher(FieryCypher)
    val REVERSE_GRAVITY = registerCypher(
        SimpleModifier("reverse_gravity", 1f)
            .attribute(CypherAttributes.GRAVITY_FACTOR, CypherAttributeOperation.MULTIPLY_TOTAL, -1.0)
            .modifier())
    val ANTIGRAVITY = registerCypher(
        SimpleModifier("antigravity", 1f)
            .attribute(CypherAttributes.GRAVITY_FACTOR, CypherAttributeOperation.ADD, -0.03)
            .modifier())
    val MANA_SURGE = registerCypher(
        SimpleModifier("mana_surge", -40f)
            .delay(4)
            .modifier())
    val HEAVILY_STRONG = registerCypher(
        SimpleModifier("heavily_strong", 20f)
            .delay(3)
            .attribute(CypherAttributes.DAMAGE, CypherAttributeOperation.ADD, 4.0)
            .attribute(CypherAttributes.SPEED, CypherAttributeOperation.MULTIPLY_TOTAL, 0.75)
            .attribute(CypherAttributes.RECOIL, CypherAttributeOperation.ADD, 4.0)
            .attribute(CypherAttributes.GRAVITY_FACTOR, CypherAttributeOperation.ADD, 0.01)
            .attribute(CypherAttributes.FRICTION_FACTOR, CypherAttributeOperation.MULTIPLY_BASE, 1.0)
            .modifier())
    val PEACEFUL = registerCypher(
        SimpleModifier("peaceful", 5f)
            .flags(CypherFlags.NO_DAMAGE)
            .modifier())
    val BOUNCY = registerCypher(
        SimpleModifier("bouncy", 5f)
            .attribute(CypherAttributes.BOUNCE, CypherAttributeOperation.ADD, 10.0)
            .modifier())
    val NO_MORE_BOUNCE = registerCypher(
        SimpleModifier("no_more_bounce", 0f)
            .attribute(CypherAttributes.BOUNCE, CypherAttributeOperation.SET_ALL, 0.0)
            .modifier())
    val NO_MORE_DAMAGE = registerCypher(
        SimpleModifier("no_more_damage", 0f)
            .attribute(CypherAttributes.DAMAGE, CypherAttributeOperation.SET_ALL, 0.0)
            .modifier())
    val EXTEND_EXISTING = registerCypher(
        SimpleModifier("extend_existing", 40f)
            .delay(5)
            .attribute(CypherAttributes.EXISTING, CypherAttributeOperation.ADD, 40.0)
            .modifier())
    val CURTAIL_EXISTING = registerCypher(
        SimpleModifier("curtail_existing", 10f)
            .delay(-3)
            .attribute(CypherAttributes.EXISTING, CypherAttributeOperation.ADD, -32.0)
            .modifier())

    val HOMING = registerCypher(HomingCypher)
    val PIERCE_ENTITY = registerCypher(
        SimpleModifier("pierce_entity", 110f)
            .flags(CypherFlags.HURT_OWNER, CypherFlags.PIERCE_ENTITY)
            .attribute(CypherAttributes.DAMAGE, CypherAttributeOperation.ADD, -5.0)
            .modifier())
    val DAEDALUS = registerCypher(DaedalusCypher)
    val NULLIFIER = registerCypher(
        SimpleModifier("nullifier", 14f)
            .delay(-4)
            .recharge(-4)
            .attribute(CypherAttributes.EXISTING, CypherAttributeOperation.SET_ALL, 1.0)
            .modifier())

    const val COLOR_MULTI_INVOKE = 0xFF4EF3D3.toInt()
    val DOUBLE_INVOKING = registerCypher(
        SimpleModifier("double_invoking", 1f)
            .draw(2)
            .color(COLOR_MULTI_INVOKE)
            .modifier())
    val TREBLE_INVOKING = registerCypher(
        SimpleModifier("treble_invoking", 5f)
            .draw(3)
            .color(COLOR_MULTI_INVOKE)
            .modifier())
    val QUADRUPLE_INVOKING = registerCypher(
        SimpleModifier("quadruple_invoking", 20f)
            .draw(4)
            .color(COLOR_MULTI_INVOKE)
            .modifier())
    val OCTUPLE_INVOKING = registerCypher(
        SimpleModifier("octuple_invoking", 50f)
            .draw(8)
            .color(COLOR_MULTI_INVOKE)
            .modifier())
    val DOUBLE_SCATTER = registerCypher(
        SimpleModifier("double_scatter", 0f)
            .draw(2)
            .color(COLOR_MULTI_INVOKE)
            .attribute(CypherAttributes.SPREAD, CypherAttributeOperation.ADD, 20.0)
            .modifier())
    val TREBLE_SCATTER = registerCypher(
        SimpleModifier("treble_scatter", 1f)
            .draw(3)
            .color(COLOR_MULTI_INVOKE)
            .attribute(CypherAttributes.SPREAD, CypherAttributeOperation.ADD, 30.0)
            .modifier())
    val QUADRUPLE_SCATTER = registerCypher(
        SimpleModifier("quadruple_scatter", 5f)
            .draw(4)
            .color(COLOR_MULTI_INVOKE)
            .attribute(CypherAttributes.SPREAD, CypherAttributeOperation.ADD, 40.0)
            .modifier())

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // passive
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // utility //////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    val INNER_FORCE = registerCypher(InnerForceCypher)
    val REFRESHER_RING = registerCypher(RefresherRingCypher)

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // other
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val ADD_TRIGGER = registerCypher(object : AbstractAddTrigger(10f) {
        override val triggerType = TriggerType.COLLISION
        override val resource = CypherNexus.modResource("add_trigger")
    })
    val ADD_TRIGGER_TIMER = registerCypher(object : AbstractAddTrigger(20f) {
        override val triggerType = TriggerType.TIMER_20
        override val resource = CypherNexus.modResource("add_trigger_timer")
    })
    val ADD_TRIGGER_DEATH = registerCypher(object : AbstractAddTrigger(20f) {
        override val triggerType = TriggerType.DEATH
        override val resource = CypherNexus.modResource("add_trigger_death")
    })
//    val ADD_TRIGGER_RED_STONE = registerCypher(object : AbstractAddTrigger(20f) {
//        override val triggerType = TriggerType.RED_STONE
//        override val resource = CypherNexus.modResource("add_trigger_red_stone")
//    })
    val REQUIREMENT_HP = registerCypher(RequirementLowHP)
    val REQUIREMENT_NOT_PLAYER = registerCypher(RequirementNotPlayer)
    val REQUIREMENT_OTHERWISE = registerCypher(AbstractRequirement.RequirementOtherwise)
    val REQUIREMENT_ENDPOINT = registerCypher(AbstractRequirement.RequirementEndpoint)
}