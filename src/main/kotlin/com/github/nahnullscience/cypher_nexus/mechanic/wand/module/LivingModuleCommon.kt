package com.github.nahnullscience.cypher_nexus.mechanic.wand.module

import com.github.nahnullscience.cypher_nexus.mechanic.entity.WandModuleStateTracker.Companion.stopPerformingModule
import com.github.nahnullscience.cypher_nexus.mechanic.entity.WandModuleStateTracker.Companion.isPerformingModule
import com.github.nahnullscience.cypher_nexus.mechanic.entity.WandModuleStateTracker.Companion.startPerformingModule
import com.github.nahnullscience.cypher_nexus.mechanic.event.CNCommonEvents
import net.minecraft.world.entity.LivingEntity

/**
 * stateless common code pieces for reusing
 * */
object LivingModuleCommon {

    inline fun startIfNotPerformingThen(living: LivingEntity, type: WandModuleType<*>, then: () -> Unit) {
        living.isPerformingModule(type).takeIf { !it }?.let {
            then()
            if (CNCommonEvents.canPerformInputModule(living, type)) {
                CNCommonEvents.inputModuleStart(living, type)
                living.startPerformingModule(type)
            }
        }
    }

    inline fun endIfPerformingThen(living: LivingEntity, type: WandModuleType<*>, then: () -> Unit) {
        living.isPerformingModule(type).takeIf { it }?.let {
            then()
            living.stopPerformingModule(type)
            CNCommonEvents.inputModuleEnd(living, type)
        }
    }
}