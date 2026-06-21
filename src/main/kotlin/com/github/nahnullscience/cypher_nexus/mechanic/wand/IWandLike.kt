package com.github.nahnullscience.cypher_nexus.mechanic.wand

import com.github.nahnullscience.cypher_nexus.init.ModDataComponents
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.HelperDataBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingState
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileStateChunk
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.ItemWandInstance
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.WandDataHighPayload
import com.github.nahnullscience.cypher_nexus.utility.mod.ArrayOfCyphers
import com.github.nahnullscience.cypher_nexus.utility.mod.PosDirePair
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
    fun getInvokePosDire(level: Level, invoker: Entity, wandLength: Float): PosDirePair


    /**  */
    fun getHelperDataBundle(level: Level, invoker: Entity, stack: ItemStack?): HelperDataBundle


    /**  */
    fun itemWandInstance(level: Level, invoker: Entity, stack: ItemStack?): ItemWandInstance?


    /**
     * resolve invoking feedback, for item-wands this is handled by [ItemWandInstance]
     * */
    fun afterInvoke(level: Level, invoker: Entity, stack: ItemStack?, dataBundle: HelperDataBundle, rootChunk: ProjectileStateChunk): InvokingState


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

        // helper should decouple with wand-instance
        val helper = InvokingHelper(
            level,
            invoker,
            wandData.highPayload.aoc,
            data,
            getInvokePosDire(level, invoker, wandData.invariable.chunkF.wandLength),
            itemWandInstance(level, invoker, stack)
        )
        helper.process()
        helper.finalizeInvoking()
        return afterInvoke(level, invoker, stack, data, helper.rootChunk)
    }



    companion object {
        fun validItemWand(stack: ItemStack): Boolean = !stack.isEmpty && stack.item is IWandLike

        fun editItemWand(stack: ItemStack, list: List<AbstractCypher>) {
            println("editWand: $stack")
            if (validItemWand(stack)) {
                stack.set(ModDataComponents.WAND_HIGH_PAYLOAD, WandDataHighPayload(ArrayOfCyphers(list)))
            }
        }

    }
}