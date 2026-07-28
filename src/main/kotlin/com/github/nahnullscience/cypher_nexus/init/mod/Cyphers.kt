package com.github.nahnullscience.cypher_nexus.init.mod

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.content.cypher.AbstractSimpleProjectile
import com.github.nahnullscience.cypher_nexus.content.cypher.AbstractSimpleProjectile.SimpleProjectile
import com.github.nahnullscience.cypher_nexus.content.cypher.AbstractSimpleProjectile.SimpleStaticProjectile
import com.github.nahnullscience.cypher_nexus.content.cypher.SimpleNonProjectileCypher
import com.github.nahnullscience.cypher_nexus.content.cypher.modifier.*
import com.github.nahnullscience.cypher_nexus.content.cypher.module.PrimaryInvokingCypher
import com.github.nahnullscience.cypher_nexus.content.cypher.other.*
import com.github.nahnullscience.cypher_nexus.content.cypher.utility.InnerForceCypher
import com.github.nahnullscience.cypher_nexus.content.cypher.utility.ProteusCypher
import com.github.nahnullscience.cypher_nexus.content.cypher.utility.RefresherRingCypher
import com.github.nahnullscience.cypher_nexus.init.ModEntities
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.*
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher.Companion.NONE_ATTR
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.AttributeOperator
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.category.CypherCategory
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractDedicatedCypherProjectile
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.TriggerType
import com.github.nahnullscience.cypher_nexus.utility.exception.CypherNotFoundException
import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.world.entity.EntityType
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.RegistryBuilder
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS
import java.awt.Color

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
    val ARROW = registerProjectile(ModEntities.CYPHER_ARROW, TriggerType.COLLISION, TriggerType.TIMER_10) {
        manaDrain(15f)
        delay(3)
        shotStateAttr(CypherAttributes.SPREAD, AttributeOperator.ADD, -16.0)
        shotStateAttr(CypherAttributes.RECOIL, AttributeOperator.ADD, 1.0)
        projectileAttr(CypherAttributes.DAMAGE, 3.0)
        projectileAttr(CypherAttributes.SPEED, 1.3)
        projectileAttr(CypherAttributes.EXISTING, 300.0)
        projectileAttr(CypherAttributes.GRAVITY_FACTOR, 0.01)
        projectileAttr(CypherAttributes.FRICTION_FACTOR, 0.01)
    }
    val SNOWBALL = registerProjectile(ModEntities.CYPHER_SNOWBALL, TriggerType.COLLISION, TriggerType.TIMER_20, TriggerType.DEATH) {
        manaDrain(5f)
        projectileAttr(CypherAttributes.SPEED, 1.2)
        projectileAttr(CypherAttributes.EXISTING, 300.0)
        projectileAttr(CypherAttributes.GRAVITY_FACTOR, 0.03)
    }
    val ENDER_TELEPORTATION = registerProjectile(ModEntities.CYPHER_ENDER_TELEPORTATION) {
        manaDrain(20f)
        flags(CypherFlags.SKIP_DAMAGE_CHECK, CypherFlags.WITH_ENDER_POWER)
        projectileAttr(CypherAttributes.SPEED, 1.6)
        projectileAttr(CypherAttributes.EXISTING, 15.0)
        projectileAttr(CypherAttributes.FRICTION_FACTOR, 0.0)
    }
    val ENDER_RECALL = registerProjectile(ModEntities.CYPHER_ENDER_RECALL) {
        manaDrain(20f)
        flags(CypherFlags.SKIP_DAMAGE_CHECK, CypherFlags.WITH_ENDER_POWER)
        projectileAttr(CypherAttributes.SPEED, 0.0)
        projectileAttr(CypherAttributes.EXISTING, 100.0)
        projectileAttr(CypherAttributes.FRICTION_FACTOR, 0.0)
    }
    val BUBBLE_COLUMN = registerProjectile(ModEntities.CYPHER_BUBBLE_COLUMN) {
        manaDrain(15f)
        delay(-3)
        recharge(-3)
        shotStateAttr(CypherAttributes.SPREAD, AttributeOperator.ADD, 20.0)
        projectileAttr(CypherAttributes.DAMAGE, 2.0)
        projectileAttr(CypherAttributes.SPEED, 1.5)
        projectileAttr(CypherAttributes.EXISTING, 180.0)
        projectileAttr(CypherAttributes.FRICTION_FACTOR, 0.2)
    }
    val LLAMA_SPIT = registerProjectile(ModEntities.CYPHER_LLAMA_SPIT) {
        manaDrain(10f)
        recharge(2)
        shotStateAttr(CypherAttributes.CRIT_CHANCE, AttributeOperator.ADD, 0.1)
        projectileAttr(CypherAttributes.DAMAGE, 1.0)
        projectileAttr(CypherAttributes.SPEED, 1.3)
        projectileAttr(CypherAttributes.EXISTING, 120.0)
        projectileAttr(CypherAttributes.GRAVITY_FACTOR, 0.06)
    }
    val SPAWN_EGG = registerProjectile(ModEntities.CYPHER_SPAWN_EGG) {
        manaDrain(20f)
        draw(1)
        flags(CypherFlags.LINGER)
        trigger(TriggerType.COLLISION)
        projectileAttr(CypherAttributes.SPEED, 1.0)
        projectileAttr(CypherAttributes.EXISTING, 300.0)
        projectileAttr(CypherAttributes.GRAVITY_FACTOR, 0.03)
    }
    val DRILLING_BOLT = registerProjectile(ModEntities.CYPHER_DRILLING_BOLT) {
        manaDrain(5f)
        delay(-3)
        shotStateAttr(CypherAttributes.SPREAD, AttributeOperator.ADD, 6.0)
        projectileAttr(CypherAttributes.DAMAGE, 1.0)
        projectileAttr(CypherAttributes.SPEED, 0.3)
        projectileAttr(CypherAttributes.EXISTING, 2.0)
        projectileAttr(CypherAttributes.GRAVITY_FACTOR, 0.02)
    }
    val DRILLING_BLAST = registerProjectile(ModEntities.CYPHER_DRILLING_BLAST) {
        manaDrain(10f)
        delay(-2)
        shotStateAttr(CypherAttributes.SPREAD, AttributeOperator.ADD, 6.0)
        projectileAttr(CypherAttributes.DAMAGE, 1.0)
        projectileAttr(CypherAttributes.SPEED, 0.3)
        projectileAttr(CypherAttributes.EXISTING, 2.0)
        projectileAttr(CypherAttributes.GRAVITY_FACTOR, 0.02)
    }
    val SMOKE_BOMB = registerProjectile(ModEntities.CYPHER_SMOKE_BOMB) {
        manaDrain(40f)
        delay(6)
        flags(CypherFlags.LINGER, CypherFlags.EXPLOSIVE)
        shotStateAttr(CypherAttributes.SPREAD, AttributeOperator.ADD, 3.0)
        projectileAttr(CypherAttributes.DAMAGE, 2.0)
        projectileAttr(CypherAttributes.CRIT_CHANCE, 0.05)
        projectileAttr(CypherAttributes.SPEED, 0.7)
        projectileAttr(CypherAttributes.EXISTING, 300.0)
        projectileAttr(CypherAttributes.GRAVITY_FACTOR, 0.03)
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // static projectile
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val EXPLOSION = registerStaticProjectile(ModEntities.CYPHER_EXPLOSION) {
        manaDrain(80f)
        delay(13)
        recharge(8)
        flags(CypherFlags.WITH_FIRE, CypherFlags.PIERCE_ENTITY)
    }
    val LIGHTING = registerStaticProjectile(ModEntities.CYPHER_LIGHTING) {
        manaDrain(110f)
        delay(15)
        recharge(10)
        flags(CypherFlags.WITH_ELECTRICITY, CypherFlags.PIERCE_ENTITY)
    }



    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // modifier
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val LIGHT = registerModifier("light", 1f) {
        flags(CypherFlags.GLOWING)
    }
    val POWER = registerModifier("power", 10f) {
        delay(1)
        shotStateAttr(CypherAttributes.DAMAGE, AttributeOperator.ADD, 1.0)
        shotStateAttr(CypherAttributes.RECOIL, AttributeOperator.ADD, 1.0)
    }
    val BLOODLUST = registerModifier("bloodlust", 5f) {
        delay(3)
        shotStateAttr(CypherAttributes.DAMAGE, AttributeOperator.ADD, 3.0)
        shotStateAttr(CypherAttributes.RECOIL, AttributeOperator.ADD, 2.0)
        flags(CypherFlags.HURT_OWNER)
    }
    val HEAVY_SHOT = registerModifier("heavy_shot", 30f) {
        delay(4)
        shotStateAttr(CypherAttributes.DAMAGE, AttributeOperator.ADD, 4.0)
        shotStateAttr(CypherAttributes.SPEED, AttributeOperator.MULTIPLY_TOTAL, 0.5)
        shotStateAttr(CypherAttributes.RECOIL, AttributeOperator.ADD, 4.0)
        shotStateAttr(CypherAttributes.KNOCKBACK, AttributeOperator.ADD, 10.0)
        shotStateAttr(CypherAttributes.EFFECT_RADIUS, AttributeOperator.MULTIPLY_BASE, 0.2)
    }
    val CRIT_STRIKE = registerModifier("critical_strike", 10f) {
        shotStateAttr(CypherAttributes.CRIT_CHANCE, AttributeOperator.ADD, 0.25)
    }
    val EFFECTIVE_RADIUS = registerModifier("effective_radius", 30f) {
        shotStateAttr(CypherAttributes.DAMAGE, AttributeOperator.ADD, 1.5)
        shotStateAttr(CypherAttributes.KNOCKBACK, AttributeOperator.ADD, 5.0)
        shotStateAttr(CypherAttributes.EFFECT_RADIUS, AttributeOperator.MULTIPLY_BASE, 0.5)
    }
    val BRISK = registerModifier("brisk", 5f) {
        shotStateAttr(CypherAttributes.SPEED, AttributeOperator.MULTIPLY_TOTAL, 2.5)
    }
    val ACCELERATING = registerModifier("accelerating", 5f) {
        shotStateAttr(CypherAttributes.SPEED, AttributeOperator.MULTIPLY_TOTAL, 0.375)
        shotStateAttr(CypherAttributes.FRICTION_FACTOR, AttributeOperator.ADD, -0.06)
    }
    val DECELERATION = registerModifier("decelerating", 5f) {
        shotStateAttr(CypherAttributes.SPEED, AttributeOperator.MULTIPLY_TOTAL, 1.625)
        shotStateAttr(CypherAttributes.FRICTION_FACTOR, AttributeOperator.ADD, 0.03)
    }
    val FIERY = registerCypher(::FieryCypher) {
        manaDrain(5f)
        flags(CypherFlags.WITH_FIRE)
    }
    val ANTIGRAVITY = registerModifier("antigravity", 2f) {
        shotStateAttr(CypherAttributes.GRAVITY_FACTOR, AttributeOperator.ADD, -0.03)
    }
    val GRAVITY = registerModifier("gravity", 2f) {
        shotStateAttr(CypherAttributes.GRAVITY_FACTOR, AttributeOperator.ADD, 0.02)
    }
    val MANA_SURGE = registerModifier("mana_surge", -40f) {
        delay(4)
    }
    val QUICK_LOAD = registerModifier("quick_load", 15f) {
        delay(-4)
        recharge(-6)
    }
    val PEACEFUL_MODE = registerModifier("peaceful_mode", 5f) {
        delay(-2)
        flags(CypherFlags.SKIP_DAMAGE_CHECK)
        shotStateAttr(CypherAttributes.RECOIL, AttributeOperator.ADD, -1.0)
        shotStateAttr(CypherAttributes.CRIT_CHANCE, AttributeOperator.SET_ALL, 0.0)
        shotStateAttr(CypherAttributes.EXISTING, AttributeOperator.ADD, 120.0)
    }
    val BOUNCY = registerModifier("bouncy", 5f) {
        shotStateAttr(CypherAttributes.BOUNCE, AttributeOperator.ADD, 10.0)
    }
    val REMOVE_BOUNCE = registerModifier("remove_bounce", 0f) {
        shotStateAttr(CypherAttributes.BOUNCE, AttributeOperator.SET_ALL, 0.0)
    }
    val REMOVE_DAMAGE = registerModifier("remove_damage", 0f) {
        shotStateAttr(CypherAttributes.DAMAGE, AttributeOperator.SET_ALL, 0.0)
    }
    val EXTEND_EXISTING = registerModifier("extend_existing", 40f) {
        delay(5)
        shotStateAttr(CypherAttributes.EXISTING, AttributeOperator.ADD, 70.0)
    }
    val CURTAIL_EXISTING = registerModifier("curtail_existing", 10f) {
        delay(-3)
        shotStateAttr(CypherAttributes.EXISTING, AttributeOperator.ADD, -30.0)
    }
    val REDUCE_SPREAD = registerModifier("reduce_spread", 1f) {
        shotStateAttr(CypherAttributes.SPREAD, AttributeOperator.ADD, -60.0)
    }
    val RANDOMIZE_SHOT = registerModifier("randomize_shot", 3f) {
        delay(-3)
        recharge(-5)
        shotStateAttr(CypherAttributes.SPREAD, AttributeOperator.ADD, 720.0)
    }
    val RECOIL = registerModifier("recoil", 5f) {
        shotStateAttr(CypherAttributes.RECOIL, AttributeOperator.ADD, 20.0)
    }
    val RECOIL_DAMPER = registerModifier("recoil_damper", 5f) {
        shotStateAttr(CypherAttributes.RECOIL, AttributeOperator.ADD, -20.0)
    }
    val KNOCKBACK = registerModifier("knockback", 5f) {
        shotStateAttr(CypherAttributes.KNOCKBACK, AttributeOperator.ADD, 20.0)
    }
    val HOMING = registerCypher(AbstractTargetHoming::Homing) {
        manaDrain(60f)
    }
    val TURN_TOWARD_TARGET = registerCypher(AbstractTargetHoming::TurnTowardTarget) {
        manaDrain(30f)
    }
    val BOOMERANG = registerCypher(::BoomerangCypher) {
        manaDrain(10f)
        flags(CypherFlags.MOTION_FOLLOWS_OWNER)
    }
    val AIMING_ARC = registerCypher(::AimingArc) {
        manaDrain(30f)
    }
    val PIERCE_ENTITY = registerModifier("pierce_entity", 110f) {
        flags(CypherFlags.HURT_OWNER, CypherFlags.PIERCE_ENTITY)
        shotStateAttr(CypherAttributes.DAMAGE, AttributeOperator.ADD, -5.0)
    }
    val DAEDALUS = registerCypher(::DaedalusCypher) {
        manaDrain(24f)
        delay(-3)
        shotStateAttr(CypherAttributes.SPEED, AttributeOperator.MULTIPLY_TOTAL, 1.25)
        shotStateAttr(CypherAttributes.RECOIL, AttributeOperator.MULTIPLY_TOTAL, 0.0)
        shotStateAttr(CypherAttributes.SPREAD, AttributeOperator.ADD, 25.0)
        shotStateAttr(CypherAttributes.GRAVITY_FACTOR, AttributeOperator.ADD, 0.03)
    }
    val NULL_EXISTING = registerModifier("null_existing", 14f) {
        delay(-4)
        recharge(-4)
        shotStateAttr(CypherAttributes.EXISTING, AttributeOperator.SET_ALL, 1.0)
    }
    val FORTUNE = registerModifier("fortune", 120f) {
        delay(12)
        recharge(12)
        shotStateAttr(CypherAttributes.FORTUNE_LEVEL, AttributeOperator.ADD, 1.0)
    }
    val HORIZONTAL_PATH = registerCypher(AbstractPathModifier::HorizontalPath) {
        manaDrain(0f)
        shotStateAttr(CypherAttributes.DAMAGE, AttributeOperator.ADD, 0.5)
    }
    val CARDINAL_PATH = registerCypher(AbstractPathModifier::CardinalPath) {
        manaDrain(0f)
        shotStateAttr(CypherAttributes.DAMAGE, AttributeOperator.ADD, 0.5)
    }
    val PING_PONG_PATH = registerCypher(::PingPongPath) {
        manaDrain(0f)
        shotStateAttr(CypherAttributes.EXISTING, AttributeOperator.ADD, 32.0)
    }
    val CHAOTIC_PATH = registerCypher(::ChaoticPath) {
        manaDrain(0f)
        shotStateAttr(CypherAttributes.SPEED, AttributeOperator.MULTIPLY_TOTAL, 2.0)
    }
    val PLANE_ORBIT = registerCypher(AbstractPathModifier::PlaneOrbit) {
        manaDrain(3f)
        delay(-5)
        flags(CypherFlags.IGNORE_BLOCK, CypherFlags.MOTION_FOLLOWS_OWNER)
        shotStateAttr(CypherAttributes.EXISTING, AttributeOperator.ADD, 40.0)
        shotStateAttr(CypherAttributes.BOUNCE, AttributeOperator.SET_ALL, 0.0)
    }
    val TRUE_ORBIT = registerCypher(AbstractPathModifier::TrueOrbit) {
        manaDrain(5f)
        delay(-4)
        flags(CypherFlags.IGNORE_BLOCK, CypherFlags.MOTION_FOLLOWS_OWNER)
        shotStateAttr(CypherAttributes.EXISTING, AttributeOperator.ADD, 40.0)
        shotStateAttr(CypherAttributes.BOUNCE, AttributeOperator.SET_ALL, 0.0)
    }
    val RED_TINT = registerModifier("red_tint", 0f) {
        delay(-1)
        dyeColor(Color.red)
    }
    val ORANGE_TINT = registerModifier("orange_tint", 0f) {
        delay(-1)
        dyeColor(Color.orange)
    }
    val YELLOW_TINT = registerModifier("yellow_tint", 0f) {
        delay(-1)
        dyeColor(Color.yellow)
    }
    val LIME_TINT = registerModifier("lime_tint", 0f) {
        delay(-1)
        dyeColor(Color.green)
    }
    val CYAN_TINT = registerModifier("cyan_tint", 0f) {
        delay(-1)
        dyeColor(Color.cyan)
    }
    val BLUE_TINT = registerModifier("blue_tint", 0f) {
        delay(-1)
        dyeColor(Color.blue)
    }
    val PURPLE_TINT = registerModifier("purple_tint", 0f) {
        delay(-1)
        dyeColor(8073150)
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // multi invoking
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    val DOUBLE_INVOKING = registerCypher("double_invoking", CypherCategories.MULTI_INVOKING) {
        manaDrain(1f)
        draw(2)
    }
    val TREBLE_INVOKING = registerCypher("treble_invoking", CypherCategories.MULTI_INVOKING) {
        manaDrain(5f)
        draw(3)
    }
    val QUADRUPLE_INVOKING = registerCypher("quadruple_invoking", CypherCategories.MULTI_INVOKING) {
        manaDrain(15f)
        draw(4)
    }
    val OCTUPLE_INVOKING = registerCypher("octuple_invoking", CypherCategories.MULTI_INVOKING) {
        manaDrain(35f)
        draw(8)
    }
    val ALL_INVOKING = registerCypher("all_invoking", CypherCategories.MULTI_INVOKING) {
        manaDrain(99f)
        draw(99)
    }
    val DOUBLE_SCATTER = registerCypher("double_scatter", CypherCategories.MULTI_INVOKING) {
        manaDrain(0f)
        draw(2)
        shotStateAttr(CypherAttributes.SPREAD, AttributeOperator.ADD, 20.0)
    }
    val TREBLE_SCATTER = registerCypher("treble_scatter", CypherCategories.MULTI_INVOKING) {
        manaDrain(1f)
        draw(3)
        shotStateAttr(CypherAttributes.SPREAD, AttributeOperator.ADD, 30.0)
    }
    val QUADRUPLE_SCATTER = registerCypher("quadruple_scatter", CypherCategories.MULTI_INVOKING) {
        manaDrain(5f)
        draw(4)
        shotStateAttr(CypherAttributes.SPREAD, AttributeOperator.ADD, 40.0)
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // utility //////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    val INNER_FORCE = registerCypher(InnerForceCypher)
    val REFRESHER_RING = registerCypher(::RefresherRingCypher) {
        manaDrain(20f)
        recharge(-10)
    }
    val PROTEUS = registerCypher(::ProteusCypher) {
        manaDrain(10f)
        draw(1)
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // other
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    val ADD_TRIGGER = registerCypher(object : AbstractAddTrigger(10f) {
        override val addTrigger = TriggerType.COLLISION
        override val resource = CypherNexus.modResource("add_trigger")
    })
    val ADD_TRIGGER_TIMER = registerCypher(object : AbstractAddTrigger(20f) {
        override val addTrigger = TriggerType.TIMER_20
        override val resource = CypherNexus.modResource("add_trigger_timer")
    })
    val ADD_TRIGGER_DEATH = registerCypher(object : AbstractAddTrigger(20f) {
        override val addTrigger = TriggerType.DEATH
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

    val ALPHA = registerCypher(AbstractGreekLetter::Alpha) {
        manaDrain(40f)
        delay(5)
    }
    val GAMMA = registerCypher(AbstractGreekLetter::Gamma) {
        manaDrain(40f)
        delay(5)
    }
    val OMEGA = registerCypher(AbstractGreekLetter::Omega) {
        manaDrain(320f)
        delay(25)
    }
    val TAU = registerCypher(AbstractGreekLetter::Tau) {
        manaDrain(90f)
        delay(10)
    }
    val MU = registerCypher(AbstractGreekLetter::Mu) {
        manaDrain(120f)
        delay(15)
        draw(1)
    }
    val PHI = registerCypher(AbstractGreekLetter::Phi) {
        manaDrain(120f)
        delay(15)
    }
    val SIGMA = registerCypher(AbstractGreekLetter::Sigma) {
        manaDrain(120f)
        delay(15)
        draw(1)
    }

    val REQUIREMENT_HP = registerCypher(RequirementLowHP)
    val REQUIREMENT_NOT_PLAYER = registerCypher(RequirementNotPlayer)
    val REQUIREMENT_ODD_HAND = registerCypher(RequirementOddHand)
    val REQUIREMENT_OTHERWISE = registerCypher(AbstractRequirement.RequirementOtherwise)
    val REQUIREMENT_ENDPOINT = registerCypher(AbstractRequirement.RequirementEndpoint)

    val CYPHER_DUPLICATION = registerCypher(::CypherDuplicationCypher) {
        manaDrain(300f)
        draw(1)
        delay(7)
        recharge(7)
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // wand module
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    val PRIMARY_INVOKING = registerCypher(PrimaryInvokingCypher)


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Suppress("UNCHECKED_CAST")
    fun <CY: AbstractCypher> registerCypher(cypher: CY): Holder<CY> {
        return DEFERRED_REGISTER.register(cypher.resource.path) { -> cypher } as Holder<CY>
    }

    /**
     * builder friendly reload
     * */
    @Suppress("UNCHECKED_CAST")
    fun <CY: AbstractCypher> registerCypher(
        constructor: (builder: CypherDataMap.Builder.() -> CypherDataMap.Builder) -> CY,
        defaultAttribute: CypherDataMap.Builder.() -> CypherDataMap.Builder = NONE_ATTR
    ): Holder<CY> = registerCypher(constructor(defaultAttribute))


    private typealias CypherProjectileHolder = DeferredHolder<EntityType<*>, out EntityType<out AbstractDedicatedCypherProjectile>>
    /**
     * Internal generic master handler that processes both standard and static projectiles,
     * handling custom triggers automatically via a unified loop.
     *
     * @param triggerOverride batch generate projectile cypher with trigger reloaded.
     * the reload names `origin` + _[TriggerType.simpleName].
     *
     * note the `cypherHolder` in respective `CypherEntity`-s is always pointed to the original one
     * @return cypher-holder of the `origin`
     */
    private inline fun <C : AbstractProjectileCypher<AbstractDedicatedCypherProjectile>, reified B : AbstractSimpleProjectile<C>> registerGenericProjectile(
        projectileHolder: CypherProjectileHolder,
        crossinline builderFactory: (String, CypherProjectileHolder) -> B,
        crossinline config: B.() -> Unit,
        vararg triggerOverride: TriggerType
    ): Holder<C> {
        // a projectile-related cypher knows the entity it can create,
        // but the cypher-projectile doesn't care who creates it.
        // this is a helper method utilizing the relation to directly tie one entity to a cypher and save boilerplate code
        val name = projectileHolder.id.path.removePrefix("cypher_")
        val simple = builderFactory(name, projectileHolder)
        simple.config()
        val base = registerCypher(simple.createProjectile())

        for (t in triggerOverride) {
            if (t != simple.trigger) {
                val n = name + "_${t.simpleName}"
                val triggered = builderFactory(n, projectileHolder)
                triggered.config()

                if (t == TriggerType.NONE) triggered.draw(0)
                else triggered.draw(1)

                triggered.manaDrain(simple.manaDrain + 20.0f)
                triggered.trigger(t)

                registerCypher(triggered.createProjectile())
            }
        }
        return base
    }

    fun registerProjectile(
        projectileHolder: CypherProjectileHolder,
        vararg triggerOverride: TriggerType,
        config: SimpleProjectile.() -> Unit,
    ): Holder<ProjectileCypher<AbstractDedicatedCypherProjectile>> =
        registerGenericProjectile(projectileHolder, ::SimpleProjectile, config, *triggerOverride)

    fun registerStaticProjectile(
        projectileHolder: CypherProjectileHolder,
        vararg triggerOverride: TriggerType,
        config: SimpleStaticProjectile.() -> Unit,
    ): Holder<StaticProjectileCypher<AbstractDedicatedCypherProjectile>> =
        registerGenericProjectile(projectileHolder, ::SimpleStaticProjectile, config, *triggerOverride)

    fun registerModifier(path: String, manaDrain: Float, config: SimpleModifier.() -> Unit): Holder<ModifierCypher> {
        val simple = SimpleModifier(path, manaDrain).also { it.config() }
        return registerCypher(simple.createModifier())
    }

    fun registerCypher(
        path: String,
        category: Holder<CypherCategory>,
        config: SimpleNonProjectileCypher.() -> Unit
    ): Holder<AbstractNonProjectileCypher> {
        val s = SimpleNonProjectileCypher(path, category).also { it.config() }
        return registerCypher(s.createCypher())
    }
}