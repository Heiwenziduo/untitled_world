package com.github.nahnullscience.cypher_nexus.mechanic.entity

import com.github.nahnullscience.cypher_nexus.init.ModDataAttachments.WAND_MODULE_STATE_TRACKER
import com.github.nahnullscience.cypher_nexus.init.mod.WandModuleTypes.MODULE_ID_CAP
import com.github.nahnullscience.cypher_nexus.init.mod.WandModuleTypes.id
import com.github.nahnullscience.cypher_nexus.init.mod.WandModuleTypes.inputModules
import com.github.nahnullscience.cypher_nexus.mechanic.wand.module.WandModuleType
import com.github.nahnullscience.cypher_nexus.utility.sideString
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.Level
import java.util.function.Supplier

/**
 * track wand module working state, for easy access of animation.
 * this class can be accessed through [LivingEntity.getData], and exist on both sides
 * */
open class WandModuleStateTracker {

    init {
//        println("/// wand-module-state creation ///")
    }

    private var flags = 0
//    private val performingTicks = Array(MODULE_ID_CAP + 1) { -1 }

    fun isPerforming(module: Supplier<out WandModuleType<*>>) = isPerforming(module.get())
    fun isPerforming(module: WandModuleType<*>) = isPerforming(module.id())
    private fun isPerforming(id: Int): Boolean = flags and (1 shl id) != 0

    private fun setPerformingState(module: WandModuleType<*>, boo: Boolean, performer: LivingEntity) {
        val id = module.id()
        val mask = (1 shl id)
        flags = if (boo) flags or mask else flags and mask.inv()
//        performingTicks[id] = if (boo) performer.tickCount else -1
    }

//    fun getPerformingTick(module: WandModuleType<*>, performer: LivingEntity): Int {
//        val id = module.id()
//        return if (!isPerforming(id)) -1
//        else performer.tickCount - performingTicks[id]
//    }

    fun startModule(type: WandModuleType<*>, performer: LivingEntity) = setPerformingState(type, true, performer)
    fun startModule(type: Supplier<out WandModuleType<*>>, performer: LivingEntity) = setPerformingState(type.get(), true, performer)
    fun endModule(type: WandModuleType<*>, performer: LivingEntity) = setPerformingState(type, false, performer)
    fun endModule(type: Supplier<out WandModuleType<*>>, performer: LivingEntity) = setPerformingState(type.get(), false, performer)

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    fun printCurrentPerforming(level: Level) {
        val inputs = inputModules.filter { type -> isPerforming(type) }
        println("${level.sideString()} current performing: " + inputs)
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    companion object {

        fun LivingEntity.isPerformingModule(module: Supplier<out WandModuleType<*>>) = isPerformingModule(module.get())
        fun LivingEntity.isPerformingModule(module: WandModuleType<*>): Boolean {
            return if (!this.hasData(WAND_MODULE_STATE_TRACKER)) false
            else this.getData(WAND_MODULE_STATE_TRACKER).isPerforming(module)
        }

        fun LivingEntity.startPerformingModule(module: Supplier<out WandModuleType<*>>) = startPerformingModule(module.get())
        fun LivingEntity.startPerformingModule(module: WandModuleType<*>) {
            this.getData(WAND_MODULE_STATE_TRACKER).setPerformingState(module, true, this)
        }

        fun LivingEntity.stopPerformingModule(module: Supplier<out WandModuleType<*>>) = stopPerformingModule(module.get())
        fun LivingEntity.stopPerformingModule(module: WandModuleType<*>) {
            this.getData(WAND_MODULE_STATE_TRACKER).setPerformingState(module, false, this)
        }

        /**
         * return corespondent hand that is performing the given module
         * @return the hand, null if living is not performing the module or this module is not performing in hand wands
         * */
//        fun LivingEntity.getModulePerformingHand(moduleType: WandModuleType<*>): InteractionHand? {
//            if (!this.hasData(WAND_MODULE_STATE_TRACKER)) {
//                println("$this no wand tracker attach")
//                return null
//            }
//
//            val data = this.getData(WAND_MODULE_STATE_TRACKER)
//            println("${level().sideString()} module state tracker: $data $moduleType")
//            if (data.isPerforming(moduleType)) {
//                println("${level().sideString()} module is performing")
//                for (hand in InteractionHand.entries) {
//                    if (moduleType.resource == WandModuleTypes.PRIMARY_RESOURCE && hand != InteractionHand.MAIN_HAND) continue
//
//                    val stack = getItemInHand(hand)
//                    if (IWandLike.validateItemWand(stack)) {
//                        println("${level().sideString()} valid wand $hand $stack")
//                        val instance = (stack.item as IWandLike).itemWandInstance(level(), this, stack) ?: continue
//                        val module = instance.getModule(moduleType)
//                        if (module != null && module !is IEmptyModule) return hand
//                    }
//                }
//            }
//            return null
//        }

        fun LivingEntity.getFirstResponsibleHandForModuleType(type: WandModuleType<*>): InteractionHand? {
            // TODO
            return null
        }

    }
}