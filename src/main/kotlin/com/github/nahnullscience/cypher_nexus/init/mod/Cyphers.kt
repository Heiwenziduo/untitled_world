package com.github.nahnullscience.cypher_nexus.init.mod

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.content.cypher.modifier.AbstractHoming
import com.github.nahnullscience.cypher_nexus.content.cypher.modifier.AbstractPathModifier
import com.github.nahnullscience.cypher_nexus.content.cypher.modifier.BoomerangCypher
import com.github.nahnullscience.cypher_nexus.content.cypher.modifier.DaedalusCypher
import com.github.nahnullscience.cypher_nexus.content.cypher.modifier.FieryCypher
import com.github.nahnullscience.cypher_nexus.content.cypher.modifier.SimpleModifier
import com.github.nahnullscience.cypher_nexus.content.cypher.module.PrimaryInvokingCypher
import com.github.nahnullscience.cypher_nexus.content.cypher.other.*
import com.github.nahnullscience.cypher_nexus.content.cypher.projectile.*
import com.github.nahnullscience.cypher_nexus.content.cypher.static_projectile.ExplosionCypher
import com.github.nahnullscience.cypher_nexus.content.cypher.utility.InnerForceCypher
import com.github.nahnullscience.cypher_nexus.content.cypher.utility.ProteusCypher
import com.github.nahnullscience.cypher_nexus.content.cypher.utility.RefresherRingCypher
import com.github.nahnullscience.cypher_nexus.init.ModEntities
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.EmptyCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ModifierCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.AttributeOperator
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.TriggerType
import com.github.nahnullscience.cypher_nexus.utility.exception.CypherNotFoundException
import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
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

    @Suppress("UNCHECKED_CAST")
    fun <CY: AbstractCypher> registerCypher(cypher: CY): Holder<CY> {
        return DEFERRED_REGISTER.register(cypher.resource.path) { -> cypher } as Holder<CY>
    }
    fun registerCypher(simpleProjectile: SimpleProjectile): Holder<ProjectileCypher> {
        return registerCypher(simpleProjectile.createProjectile())
    }
    fun registerCypher(simpleModifier: SimpleModifier): Holder<ModifierCypher> {
        return registerCypher(simpleModifier.createModifier())
    }

    fun getCypher(resource: Identifier): AbstractCypher? = REGISTRY.getValue(resource)
    fun getCypherOrThrow(resource: Identifier): AbstractCypher {
        return getCypher(resource) ?:
        throw CypherNotFoundException("missing cypher: ${resource.namespace}-${resource.path}")
    }


    // technical
    val EMPTY_CYPHER = registerCypher(EmptyCypher)

    // ==========================================================================================
    // # will present in register order #
    // ==========================================================================================

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // projectile
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val ARROW = registerCypher(
        SimpleProjectile("arrow", ModEntities.CYPHER_ARROW)
            .manaDrain(10f)
            .delay(2)
            .projectileAttr(CypherAttributes.DAMAGE, 3.0)
            .projectileAttr(CypherAttributes.SPEED, 1.3)
            .projectileAttr(CypherAttributes.EXISTING, 300.0)
            .projectileAttr(CypherAttributes.GRAVITY_FACTOR, 0.01)
    )
    val SNOWBALL = registerCypher(
        SimpleProjectile("snowball", ModEntities.CYPHER_SNOWBALL)
            .manaDrain(3f)
            .projectileAttr(CypherAttributes.SPEED, 1.2)
            .projectileAttr(CypherAttributes.EXISTING, 300.0)
            .projectileAttr(CypherAttributes.GRAVITY_FACTOR, 0.03)
    )
    val ENDER_TELEPORTATION = registerCypher(
        SimpleProjectile("ender_teleportation", ModEntities.CYPHER_ENDER_TELEPORTATION)
            .manaDrain(20f)
            .flags(CypherFlags.SKIP_DAMAGE_CHECK)
            .projectileAttr(CypherAttributes.SPEED, 1.6)
            .projectileAttr(CypherAttributes.EXISTING, 15.0)
            .projectileAttr(CypherAttributes.FRICTION_FACTOR, 0.0)
    )
    val ENDER_RECALL = registerCypher(
        SimpleProjectile("ender_recall", ModEntities.CYPHER_ENDER_RECALL)
            .manaDrain(25f)
            .flags(CypherFlags.SKIP_DAMAGE_CHECK)
            .projectileAttr(CypherAttributes.SPEED, 1.6)
            .projectileAttr(CypherAttributes.EXISTING, 15.0)
            .projectileAttr(CypherAttributes.FRICTION_FACTOR, 0.0)
    )
    val BUBBLE_COLUMN = registerCypher(
        SimpleProjectile("bubble_column", ModEntities.CYPHER_BUBBLE_COLUMN)
            .manaDrain(15f)
            .recharge(2)
            .projectileAttr(CypherAttributes.DAMAGE, 1.0)
            .projectileAttr(CypherAttributes.SPEED, 1.3)
            .projectileAttr(CypherAttributes.EXISTING, 120.0)
    )
    val LLAMA_SPIT = registerCypher(
        SimpleProjectile("llama_spit", ModEntities.CYPHER_LLAMA_SPIT)
            .manaDrain(5f)
            .recharge(2)
            .stateChunkAttr(CypherAttributes.CRIT_CHANCE, AttributeOperator.ADD, 0.05)
            .projectileAttr(CypherAttributes.DAMAGE, 1.0)
            .projectileAttr(CypherAttributes.SPEED, 1.3)
            .projectileAttr(CypherAttributes.EXISTING, 120.0)
            .projectileAttr(CypherAttributes.GRAVITY_FACTOR, 0.06)
    )
    val SPAWN_EGG = registerCypher(SpawnEggCypher)

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // static projectile
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val EXPLOSION = registerCypher(ExplosionCypher)

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // modifier
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val POWER = registerCypher(
        SimpleModifier("power", 10f)
            .delay(1)
            .attribute(CypherAttributes.DAMAGE, AttributeOperator.ADD, 1.0)
            .attribute(CypherAttributes.RECOIL, AttributeOperator.ADD, 1.0))
    val BLOODLUST = registerCypher(
        SimpleModifier("bloodlust", 20f)
            .delay(3)
            .attribute(CypherAttributes.DAMAGE, AttributeOperator.ADD, 3.0)
            .attribute(CypherAttributes.RECOIL, AttributeOperator.ADD, 2.0)
            .flags(CypherFlags.HURT_OWNER))
    val HEAVY_SHOT = registerCypher(
        SimpleModifier("heavy_shot", 30f)
            .delay(4)
            .attribute(CypherAttributes.DAMAGE, AttributeOperator.ADD, 4.0)
            .attribute(CypherAttributes.SPEED, AttributeOperator.MULTIPLY_TOTAL, 0.75)
            .attribute(CypherAttributes.RECOIL, AttributeOperator.ADD, 4.0)
            .attribute(CypherAttributes.KNOCKBACK, AttributeOperator.ADD, 1.0))
    val CRIT_STRIKE = registerCypher(
        SimpleModifier("critical_strike", 10f)
            .attribute(CypherAttributes.CRIT_CHANCE, AttributeOperator.ADD, 0.25))
    val BRISK = registerCypher(
        SimpleModifier("brisk", 5f)
            .attribute(CypherAttributes.SPEED, AttributeOperator.MULTIPLY_TOTAL, 2.5))
    val ACCELERATING = registerCypher(
        SimpleModifier("accelerating", 5f)
            .attribute(CypherAttributes.SPEED, AttributeOperator.MULTIPLY_TOTAL, 0.375)
            .attribute(CypherAttributes.FRICTION_FACTOR, AttributeOperator.ADD, -0.06))
    val DECELERATION = registerCypher(
        SimpleModifier("decelerating", 5f)
            .attribute(CypherAttributes.SPEED, AttributeOperator.MULTIPLY_TOTAL, 1.625)
            .attribute(CypherAttributes.FRICTION_FACTOR, AttributeOperator.ADD, 0.03))
    val FIERY = registerCypher(FieryCypher)

    val ANTIGRAVITY = registerCypher(
        SimpleModifier("antigravity", 2f)
            .attribute(CypherAttributes.GRAVITY_FACTOR, AttributeOperator.ADD, -0.03))
    val GRAVITY = registerCypher(
        SimpleModifier("gravity", 2f)
            .attribute(CypherAttributes.GRAVITY_FACTOR, AttributeOperator.ADD, 0.02))
    val MANA_SURGE = registerCypher(
        SimpleModifier("mana_surge", -40f)
            .delay(5))
    val QUICK_LOAD = registerCypher(
        SimpleModifier("quick_load", 15f)
            .delay(-3)
            .recharge(-6))
    val PEACEFUL = registerCypher(
        SimpleModifier("peaceful", 5f)
            .flags(CypherFlags.SKIP_DAMAGE_CHECK))
    val BOUNCY = registerCypher(
        SimpleModifier("bouncy", 5f)
            .attribute(CypherAttributes.BOUNCE, AttributeOperator.ADD, 10.0))
    val REMOVE_BOUNCE = registerCypher(
        SimpleModifier("remove_bounce", 0f)
            .attribute(CypherAttributes.BOUNCE, AttributeOperator.SET_ALL, 0.0))
    val REMOVE_DAMAGE = registerCypher(
        SimpleModifier("remove_damage", 0f)
            .attribute(CypherAttributes.DAMAGE, AttributeOperator.SET_ALL, 0.0))
    val EXTEND_EXISTING = registerCypher(
        SimpleModifier("extend_existing", 40f)
            .delay(5)
            .attribute(CypherAttributes.EXISTING, AttributeOperator.ADD, 42.0))
    val CURTAIL_EXISTING = registerCypher(
        SimpleModifier("curtail_existing", 10f)
            .delay(-3)
            .attribute(CypherAttributes.EXISTING, AttributeOperator.ADD, -38.0))
    val REDUCE_SPREAD = registerCypher(
        SimpleModifier("reduce_spread", 1f)
            .attribute(CypherAttributes.SPREAD, AttributeOperator.ADD, -60.0))
    val RANDOMIZE_SHOT = registerCypher(
        SimpleModifier("randomize_shot", 3f)
            .delay(-3)
            .recharge(-5)
            .attribute(CypherAttributes.SPREAD, AttributeOperator.ADD, 720.0))
    val RECOIL = registerCypher(
        SimpleModifier("recoil", 5f)
            .attribute(CypherAttributes.RECOIL, AttributeOperator.ADD, 20.0))
    val RECOIL_DAMPER = registerCypher(
        SimpleModifier("recoil_damper", 5f)
            .attribute(CypherAttributes.RECOIL, AttributeOperator.ADD, -20.0))
    val KNOCKBACK = registerCypher(
        SimpleModifier("knockback", 5f)
            .attribute(CypherAttributes.KNOCKBACK, AttributeOperator.ADD, 10.0))
    val HOMING = registerCypher(AbstractHoming.Homing)
    val TURN_TO_TARGET = registerCypher(AbstractHoming.TurnToTarget)
    val BOOMERANG = registerCypher(BoomerangCypher)
    val PIERCE_ENTITY = registerCypher(
        SimpleModifier("pierce_entity", 110f)
            .flags(CypherFlags.HURT_OWNER, CypherFlags.PIERCE_ENTITY)
            .attribute(CypherAttributes.DAMAGE, AttributeOperator.ADD, -5.0))
    val DAEDALUS = registerCypher(DaedalusCypher)
    val NULLIFIER = registerCypher(
        SimpleModifier("nullifier", 14f)
            .delay(-4)
            .recharge(-4)
            .attribute(CypherAttributes.EXISTING, AttributeOperator.SET_ALL, 1.0))
    val FORTUNE = registerCypher(
        SimpleModifier("fortune", 180f)
            .delay(16)
            .recharge(10)
            .attribute(CypherAttributes.FORTUNE_LEVEL, AttributeOperator.ADD, 1.0))

    val HORIZONTAL_PATH = registerCypher(AbstractPathModifier.HorizontalPath)
    val CARDINAL_PATH = registerCypher(AbstractPathModifier.CardinalPath)

    const val COLOR_MULTI_INVOKE = 0xFFADEEC5.toInt()
    val DOUBLE_INVOKING = registerCypher(
        SimpleModifier("double_invoking", 1f)
            .draw(2)
            .color(COLOR_MULTI_INVOKE))
    val TREBLE_INVOKING = registerCypher(
        SimpleModifier("treble_invoking", 5f)
            .draw(3)
            .color(COLOR_MULTI_INVOKE))
    val QUADRUPLE_INVOKING = registerCypher(
        SimpleModifier("quadruple_invoking", 20f)
            .draw(4)
            .color(COLOR_MULTI_INVOKE))
    val OCTUPLE_INVOKING = registerCypher(
        SimpleModifier("octuple_invoking", 50f)
            .draw(8)
            .color(COLOR_MULTI_INVOKE))
    val ALL_INVOKING = registerCypher(
        SimpleModifier("all_invoking", 200f)
            .draw(99)
            .color(COLOR_MULTI_INVOKE))
    val DOUBLE_SCATTER = registerCypher(
        SimpleModifier("double_scatter", 0f)
            .draw(2)
            .color(COLOR_MULTI_INVOKE)
            .attribute(CypherAttributes.SPREAD, AttributeOperator.ADD, 20.0))
    val TREBLE_SCATTER = registerCypher(
        SimpleModifier("treble_scatter", 1f)
            .draw(3)
            .color(COLOR_MULTI_INVOKE)
            .attribute(CypherAttributes.SPREAD, AttributeOperator.ADD, 30.0))
    val QUADRUPLE_SCATTER = registerCypher(
        SimpleModifier("quadruple_scatter", 5f)
            .draw(4)
            .color(COLOR_MULTI_INVOKE)
            .attribute(CypherAttributes.SPREAD, AttributeOperator.ADD, 40.0))

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // wand module
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    val PRIMARY_INVOKING = registerCypher(PrimaryInvokingCypher)

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // utility //////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    val INNER_FORCE = registerCypher(InnerForceCypher)
    val REFRESHER_RING = registerCypher(RefresherRingCypher)
    val PROTEUS = registerCypher(ProteusCypher)

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

    val D2 = registerCypher(AbstractDivideBy.D2)
    val D3 = registerCypher(AbstractDivideBy.D3)
    val D4 = registerCypher(AbstractDivideBy.D4)
    val D10 = registerCypher(AbstractDivideBy.D10)

    val ALPHA = registerCypher(AbstractGreekLetter.Alpha)
    val GAMMA = registerCypher(AbstractGreekLetter.Gamma)
    val OMEGA = registerCypher(AbstractGreekLetter.Omega)
    val TAU = registerCypher(AbstractGreekLetter.Tau)

    val REQUIREMENT_HP = registerCypher(RequirementLowHP)
    val REQUIREMENT_NOT_PLAYER = registerCypher(RequirementNotPlayer)
    val REQUIREMENT_ODD_HAND = registerCypher(RequirementOddHand)
    val REQUIREMENT_OTHERWISE = registerCypher(AbstractRequirement.RequirementOtherwise)
    val REQUIREMENT_ENDPOINT = registerCypher(AbstractRequirement.RequirementEndpoint)

    val CYPHER_DUPLICATION = registerCypher(CypherDuplicationCypher)

}