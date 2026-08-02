package com.github.nahnullscience.cypher_nexus.init.mod

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.LifeCycle.getIdOfBound
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.WandModuleType
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.AbstractInputModule
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.AbstractWandModule
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.types.*
import net.minecraft.core.Registry
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.RegistryBuilder
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS
import java.util.function.Supplier
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

object WandModuleTypes {
    const val MODULE_ID_CAP = 31
    val RESOURCE_KEY: ResourceKey<Registry<WandModuleType<*>>> =
        ResourceKey.createRegistryKey(CypherNexus.modResource("wand/module_type"))
    val REGISTRY: Registry<WandModuleType<*>> = RegistryBuilder(RESOURCE_KEY).sync(true).maxId(MODULE_ID_CAP).create()

    fun WandModuleType<*>.id(): Int = REGISTRY.getIdOfBound(this, MODULE_ID_CAP)

    val DEFERRED_REGISTER: DeferredRegister<WandModuleType<*>> =
        DeferredRegister.create(REGISTRY, CypherNexus.MOD_ID)

    fun register() {
        DEFERRED_REGISTER.register(MOD_BUS)
    }

    fun <T : AbstractWandModule> registerModule(module: WandModuleType<T>): Supplier<out WandModuleType<T>> {
        return DEFERRED_REGISTER.register(module.resource.path) { resource -> module }
    }

    fun <T : AbstractWandModule> registerModule(resource: Identifier, module: KClass<T>): Supplier<out WandModuleType<T>> {
        return registerModule(WandModuleType(resource, module))
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     *
     * */
    @Suppress("UNCHECKED_CAST")
    val inputModules: List<WandModuleType<AbstractInputModule>> get() {
        return _inputModuleBacking ?: run {
            REGISTRY.filter { type ->
                type.module.isSubclassOf(AbstractInputModule::class)
            }.map {
                // is there a way to get the same holder reference as we registered below?
                it as WandModuleType<AbstractInputModule>
            }.toList().also { _inputModuleBacking = it }
        }
    }
    private var _inputModuleBacking: List<WandModuleType<AbstractInputModule>>? = null


    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    val PRIMARY_RESOURCE = CypherNexus.modResource("input_primary")
    val SECONDARY_RESOURCE = CypherNexus.modResource("input_secondary")
    val SPECIAL_RESOURCE = CypherNexus.modResource("input_special")

    val INVOKE_RESOURCE = CypherNexus.modResource("function_invoke")
    val RECOIL_RESOURCE = CypherNexus.modResource("function_recoil")
    val MANA_SHIELD_RESOURCE = CypherNexus.modResource("function_mana_shield")

    val ATTRIBUTE_RESOURCE = CypherNexus.modResource("attribute")

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    val PRIMARY_MODULE = registerModule(PRIMARY_RESOURCE, AbstractPrimaryInputModule::class)
    val SECONDARY_MODULE = registerModule(SECONDARY_RESOURCE, AbstractSecondaryInputModule::class)
    val SPECIAL_MODULE = registerModule(SPECIAL_RESOURCE, AbstractSpecialInputModule::class)

    val INVOKE_MODULE = registerModule(INVOKE_RESOURCE, AbstractInvokeFunctionModule::class)
    val RECOIL_MODULE = registerModule(RECOIL_RESOURCE, AbstractRecoilFunctionModule::class)
    val MANA_SHIELD_MODULE = registerModule(MANA_SHIELD_RESOURCE, AbstractManaShieldFunctionModule::class)



    ///////////////////////////////////////////////////////////////////////////////////////////////////////

}