package com.github.nahnullscience.cypher_nexus.mechanic.wand.module

import com.github.nahnullscience.cypher_nexus.init.mod.WandModuleTypes.INVOKE_MODULE
import com.github.nahnullscience.cypher_nexus.init.mod.WandModuleTypes.RECOIL_MODULE
import com.github.nahnullscience.cypher_nexus.init.mod.WandModuleTypes.SECONDARY_MODULE
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.ItemWandInstance
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.AbstractWandModule
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.ITypeUniqueModule
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.concrete.DefaultInvokeModule
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.concrete.DefaultRecoilModule
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.concrete.DefaultSecondaryInput
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.types.AbstractInvokeFunctionModule
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.types.AbstractRecoilFunctionModule
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.types.AbstractSecondaryInputModule
import java.util.function.Supplier

/**
 * one slot one unique module on map
 * */
class ModuleSlot <out Module> (
    val type: Supplier<out WandModuleType<Module>>,
    val factory: (instance: ItemWandInstance) -> Module
) where Module : AbstractWandModule, Module : ITypeUniqueModule {

    companion object {
        private var _defaultInvokingBacking: ModuleSlot<AbstractInvokeFunctionModule>? = null
        val DEFAULT_INVOKING: ModuleSlot<AbstractInvokeFunctionModule> get() {
            return _defaultInvokingBacking ?:
            ModuleSlot(INVOKE_MODULE, ::DefaultInvokeModule).also { _defaultInvokingBacking = it }
        }

        private var _defaultRecoilBacking: ModuleSlot<AbstractRecoilFunctionModule>? = null
        val DEFAULT_RECOIL: ModuleSlot<AbstractRecoilFunctionModule> get() {
            return _defaultRecoilBacking ?:
            ModuleSlot(RECOIL_MODULE, ::DefaultRecoilModule).also { _defaultRecoilBacking = it }
        }

        private var _defaultSecondaryBacking: ModuleSlot<AbstractSecondaryInputModule>? = null
        val DEFAULT_SECONDARY: ModuleSlot<AbstractSecondaryInputModule> get() {
            return _defaultSecondaryBacking ?:
            ModuleSlot(SECONDARY_MODULE, ::DefaultSecondaryInput).also { _defaultSecondaryBacking = it }
        }
    }
}