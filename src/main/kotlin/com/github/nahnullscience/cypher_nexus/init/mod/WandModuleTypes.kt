package com.github.nahnullscience.cypher_nexus.init.mod

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.*
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.IWandModule
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.InputModule
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.WandModuleType
import com.github.nahnullscience.cypher_nexus.utility.exception.VanillaMisuseException
import net.minecraft.core.Registry
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.RegistryBuilder
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS
import java.util.function.Supplier
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.streams.toList

object WandModuleTypes {
    const val MODULE_ID_CAP = 31
    val RESOURCE_KEY: ResourceKey<Registry<WandModuleType<*>>> =
        ResourceKey.createRegistryKey(CypherNexus.modResource("wand/module_type"))
    val REGISTRY: Registry<WandModuleType<*>> = RegistryBuilder(RESOURCE_KEY).sync(true).maxId(MODULE_ID_CAP).create()

    fun WandModuleType<*>.id(): Int {
        return REGISTRY.getId(this).also {
            if (it > MODULE_ID_CAP) throw VanillaMisuseException("[${REGISTRY}] registry id-$it is out of bound $MODULE_ID_CAP")
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

    fun <T : IWandModule> registerModule(resource: Identifier, module: KClass<T>): Supplier<out WandModuleType<T>> {
        return registerModule(WandModuleType(resource, module))
    }


    val PRIMARY_RESOURCE = CypherNexus.modResource("primary")
    val SECONDARY_RESOURCE = CypherNexus.modResource("secondary")
    val SPECIAL_RESOURCE = CypherNexus.modResource("special")

    val RECOIL_RESOURCE = CypherNexus.modResource("recoil")
    val MANA_SHIELD_RESOURCE = CypherNexus.modResource("mana_shield")
    val ATTRIBUTE_RESOURCE = CypherNexus.modResource("attribute")

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    val PRIMARY = registerModule(PRIMARY_RESOURCE, AbstractPrimaryModule::class)
    val SECONDARY = registerModule(SECONDARY_RESOURCE, AbstractSecondaryModule::class)
    val SPECIAL = registerModule(SPECIAL_RESOURCE, AbstractSpecialModule::class)

    val RECOIL = registerModule(RECOIL_RESOURCE, AbstractRecoilModule::class)
    val MANA_SHIELD = registerModule(MANA_SHIELD_RESOURCE, AbstractManaShieldModule::class)


    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    private var _inputModuleBacking: List<Supplier<out WandModuleType<InputModule>>>? = null
    val inputModules: List<Supplier<out WandModuleType<InputModule>>> get() {
        return _inputModuleBacking ?: run {
            REGISTRY.filter { type ->
                type.module.isSubclassOf(InputModule::class)
            }.map {
                Supplier { -> it } as Supplier<WandModuleType<InputModule>>
            }.toList().also { _inputModuleBacking = it }
        }
    }
}