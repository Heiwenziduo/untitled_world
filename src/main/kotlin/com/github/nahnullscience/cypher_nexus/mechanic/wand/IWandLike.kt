package com.github.nahnullscience.cypher_nexus.mechanic.wand

import com.github.nahnullscience.cypher_nexus.init.ModDataComponents
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.HelperDataBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingState
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.ItemWandInstance
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.WandDataBundle
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.WandDataHighPayload
import com.github.nahnullscience.cypher_nexus.utility.mod.ArrayOfCyphers
import com.github.nahnullscience.cypher_nexus.utility.mod.PosDirePair
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

/**
 * Any Item/Entity implemented the interface here should be able to conduct the power of cyphers through #tryConduct
 * */
interface IWandLike {

    /** for item implementations to determine whether it can be modified in cypher-index */
    val isEditableWand: Boolean


    /**
     * @param stack item wand if any
     * @param entityWand entity invoker if any
     * */
    fun <EntityWand> getWandData(stack: ItemStack?, entityWand: EntityWand?): WandDataBundle?
    where EntityWand : Entity, EntityWand: IWandLike


    /**  */
    fun checkInvokingPrerequisites(level: Level, invoker: Entity, stack: ItemStack?): Boolean


    /**
     * determine the initial position & direction of the projectile,
     * will be further processed if hooks are present.
     * direction doesn't have to be normalized
     * */
    fun getInvokePosDire(level: Level, invoker: Entity, stack: ItemStack?): PosDirePair


    /**  */
    fun getHelperDataBundle(level: Level, invoker: Entity, stack: ItemStack?): HelperDataBundle


    /**  */
    fun itemWandInstance(level: Level, invoker: Entity, stack: ItemStack?): ItemWandInstance?


    /**
     * resolve invoking feedback, for item-wands this is handled by [ItemWandInstance]
     * */
    fun afterInvoke(level: Level, invoker: Entity, stack: ItemStack?, dataBundle: HelperDataBundle, rootChunk: ShotStateChunk): InvokingState


    /**
     * call on BOTH sides.
     * server side is responsible for projectile generation, authorise mana / deck / delay check.
     * client side is for user info overlay, and wand module functions.
     * */
    fun tryInvoke(level: Level, invoker: Entity, stack: ItemStack?): InvokingState {

        if (!checkInvokingPrerequisites(level, invoker, stack)) return InvokingState.LOADING

        val entityWand = if (invoker is IWandLike) invoker else null
        val wandData = getWandData(stack, entityWand) ?: return InvokingState.MISSING_DATA

        val data = getHelperDataBundle(level, invoker, stack)
        val helper = InvokingHelper(wandData.highPayload.aoc, data, invoker = invoker)


//        scope.launch { // TODO if async #checkInvokingPrerequisites should handle "pending" state
//            helper.process()
//            helper.finalizeInvoking(
//                level,
//                getInvokePosDire(level, invoker),
//                itemWandInstance(level, invoker, stack)
//            )
//            afterInvoke(level, invoker, stack, data, helper.rootChunk)
//            return InvokingState.HANG
//        }


        helper.processSync()
        helper.finalizeInvoking(
            level,
            getInvokePosDire(level, invoker, stack),
            itemWandInstance(level, invoker, stack)
        )
        return afterInvoke(level, invoker, stack, data, helper.shotRoot)

    }



    companion object {
//        private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        private val scope = CoroutineScope(Dispatchers.Default)

        /**
         * @return whether the item behind the stack is a decent [IWandLike]
         * */
        fun validateItemWand(stack: ItemStack): Boolean = !stack.isEmpty && stack.item is IWandLike

        fun editItemWand(stack: ItemStack, list: List<AbstractCypher>) {
            println("editWand: $stack")
            if (validateItemWand(stack)) {
                stack.set(ModDataComponents.WAND_HIGH_PAYLOAD, WandDataHighPayload(ArrayOfCyphers(list)))
            }
        }

    }
}