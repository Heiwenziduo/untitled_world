package com.github.nahnullscience.cypher_nexus.mechanic.wand.module

import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.IWandModule
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.ModuleCategory
import com.github.nahnullscience.cypher_nexus.utility.mod.PosDirePair
import net.minecraft.world.entity.Entity

abstract class AbstractRecoilModule: IWandModule {
    final override val category = ModuleCategory.RECOIL

    abstract fun recoil(invoker: Entity, recoil: Double, invokePosDire: PosDirePair)

}