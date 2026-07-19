package com.github.nahnullscience.cypher_nexus.init.mod

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.*
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.IWandModule
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.InputModule
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.WandModuleType
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.RegistryBuilder
import org.apache.logging.log4j.Level
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS
import java.util.function.Supplier

object WandModuleTypes {
    const val ID_CAP = 31
    val RESOURCE_KEY: ResourceKey<Registry<WandModuleType<*>>> =
        ResourceKey.createRegistryKey(CypherNexus.modResource("wand/module_type"))
    val REGISTRY: Registry<WandModuleType<*>> = RegistryBuilder(RESOURCE_KEY).sync(true).maxId(ID_CAP).create()

    fun WandModuleType<*>.id(): Int {
        return REGISTRY.getId(this).also {
            if (it > ID_CAP) CypherNexus.debugWand(Level.ERROR)
            { "$this $it: wand-module-type registry length is over $ID_CAP! this may lead to modules function unproperly." }
        }
    }

    val DEFERRED_REGISTER: DeferredRegister<WandModuleType<*>> =
        DeferredRegister.create(REGISTRY, CypherNexus.MOD_ID)

    fun register() {
        DEFERRED_REGISTER.register(MOD_BUS)
    }

    fun <T : IWandModule> registerModule(module: WandModuleType<T>): Supplier<out WandModuleType<T>> {
        return DEFERRED_REGISTER.register(module.resource.path) { resource -> module }
    }


    val PRIMARY_RESOURCE = CypherNexus.modResource("primary")
    val SECONDARY_RESOURCE = CypherNexus.modResource("secondary")
    val SPECIAL_RESOURCE = CypherNexus.modResource("special")

    val RECOIL_RESOURCE = CypherNexus.modResource("recoil")
    val MANA_SHIELD_RESOURCE = CypherNexus.modResource("mana_shield")
    val ATTRIBUTE_RESOURCE = CypherNexus.modResource("attribute")

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    val PRIMARY = registerModule(WandModuleType<AbstractPrimaryModule>(PRIMARY_RESOURCE))
    val SECONDARY = registerModule(WandModuleType<AbstractSecondaryModule>(SECONDARY_RESOURCE))
    val SPECIAL = registerModule(WandModuleType<AbstractSpecialModule>(SPECIAL_RESOURCE))

    val RECOIL = registerModule(WandModuleType<AbstractRecoilModule>(RECOIL_RESOURCE))
    val MANA_SHIELD = registerModule(WandModuleType<AbstractManaShieldModule>(MANA_SHIELD_RESOURCE))


    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    // TODO should be a better way to handle this
    val inputModules = mutableSetOf<Supplier<out WandModuleType<InputModule>>>()
    init {
        inputModules.add(PRIMARY)
        inputModules.add(SECONDARY)
        inputModules.add(SPECIAL)
    }
}