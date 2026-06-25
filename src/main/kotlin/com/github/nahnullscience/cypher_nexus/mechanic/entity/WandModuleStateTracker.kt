package com.github.nahnullscience.cypher_nexus.mechanic.entity

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.ModDataAttachments.WAND_MODULE_STATE_TRACKER
import com.github.nahnullscience.cypher_nexus.init.mod.WandModuleTypes
import com.github.nahnullscience.cypher_nexus.init.mod.WandModuleTypes.id
import com.github.nahnullscience.cypher_nexus.init.mod.WandModuleTypes.inputModules
import com.github.nahnullscience.cypher_nexus.mechanic.wand.IWandLike
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.IEmptyModule
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.component.WandModuleType
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity
import net.neoforged.fml.common.EventBusSubscriber
import java.util.function.Supplier

/**
 * track wand module working state, for easy access of animation.
 * this class can be accessed through [LivingEntity.getData], and exist on both sides
 * */
open class WandModuleStateTracker {

    companion object {
        fun LivingEntity.isPerformingModule(module: Supplier<out WandModuleType<*>>) = isPerformingModule(module.get())
        fun LivingEntity.isPerformingModule(module: WandModuleType<*>): Boolean {
            return if (this.hasData(WAND_MODULE_STATE_TRACKER)) {
                this.getData(WAND_MODULE_STATE_TRACKER).isPerforming(module)
            } else false
        }

        fun LivingEntity.setPerformingModule(module: Supplier<out WandModuleType<*>>, state: Boolean) = setPerformingModule(module.get(), state)
        fun LivingEntity.setPerformingModule(module: WandModuleType<*>, state: Boolean) {
            this.getData(WAND_MODULE_STATE_TRACKER).setPerformingState(module, state)
        }

        /**
         * return corespondent hand that is performing the given module
         * @return the hand, null if living is not performing the module or this module is not performing in hand wands
         * */
        fun LivingEntity.getModulePerformingHand(module: WandModuleType<*>): InteractionHand? {
            if (!this.hasData(WAND_MODULE_STATE_TRACKER)) return null

            val data = this.getData(WAND_MODULE_STATE_TRACKER)
            if (data.isPerforming(module)) {
                for (hand in InteractionHand.entries) {
                    if (module.resource == WandModuleTypes.PRIMARY_RESOURCE && hand != InteractionHand.MAIN_HAND) continue

                    val stack = getItemInHand(hand)
                    if (IWandLike.validateItemWand(stack)) {
                        val instance = (stack.item as IWandLike).itemWandInstance(level(), this, stack) ?: continue
                        val module = instance.module(module)
                        if (module != null && module !is IEmptyModule) return hand
                    }
                }
            }
            return null
        }


    }
    private var flags = 0

    fun isPerforming(module: Supplier<out WandModuleType<*>>) = isPerforming(module.get())
    fun isPerforming(module: WandModuleType<*>) : Boolean {
        return flags and (1 shl module.id()) != 0
    }

    fun setPerformingState(module: Supplier<out WandModuleType<*>>, boo: Boolean) = setPerformingState(module.get(), boo)
    fun setPerformingState(module: WandModuleType<*>, boo: Boolean) {
        val mask = (1 shl module.id())
        flags = if (boo) {
            flags or mask
        } else {
            flags and mask.inv()
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    fun printCurrentPerforming() {
        val inputs = inputModules.toMutableList()
        inputs.removeIf { type -> isPerforming(type) }
        println(inputs.map { type -> type.get() })
    }
}