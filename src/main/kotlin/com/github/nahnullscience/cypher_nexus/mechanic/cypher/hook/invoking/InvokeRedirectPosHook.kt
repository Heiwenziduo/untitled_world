package com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.invoking

import com.github.nahnullscience.cypher_nexus.utility.mod.PosDirePair
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity

interface InvokeRedirectPosHook {
    fun redirectPosDireServer(level: ServerLevel, invoker: LivingEntity?, strength: Int, pair: PosDirePair, index: Int): PosDirePair
}