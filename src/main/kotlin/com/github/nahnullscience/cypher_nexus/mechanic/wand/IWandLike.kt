package com.github.nahnullscience.cypher_nexus.mechanic.wand

import com.github.nahnullscience.cypher_nexus.init.ModDataAttachments.WAND_DATA_MAP
import com.github.nahnullscience.cypher_nexus.init.ModDataComponents
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.WandDataHighPayload
import com.github.nahnullscience.cypher_nexus.network.client.ClientboundSyncWandInstance
import com.github.nahnullscience.cypher_nexus.utility.mod.ArrayOfCyphers
import com.github.nahnullscience.cypher_nexus.utility.mod.PosDirePair
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.neoforged.neoforge.network.PacketDistributor

/**
 * ---casting logics here---
 * Any Item/Entity implemented the interface here should be able to conduct the power of cyphers #tryConduct
 * */
interface IWandLike {

    /** itemStack Or entityInvoker */
    fun getWandData(stack: ItemStack?, invoker: Entity?): WandDataBundle?

    /** direction doesn't have to be normalized */
    fun getInvokePosDire(level: Level, invoker: Entity, wandLength: Float): PosDirePair

    /** for item implementations to determine whether it can be modified in cypher-index */
    val isEditableWand: Boolean


    /**
     * call on BOTH sides.
     * server side is responsible for projectile generation, authorise mana / deck / delay check.
     * client side is for user info overlay, and wand module functions.
     * TODO let fake-player/machine can cast cyphers
     * */
    fun tryConduct(level: Level, invoker: Entity, stack: ItemStack?): Boolean {

        val wandData = getWandData(stack, invoker) ?: return false

        // data-attach (create if not present)
        val instance = invoker.getData(WAND_DATA_MAP).getOrPutInstance(wandData, this, level)

        if (!instance.canInvoke()) {
//            println("casting rejected due to: $instance")
            return false
        }

        // TODO consider move these inside Instance logic
        val helperBundle = instance.toHelperDataBundle()
        val helper = InvokingHelper(
            level,
            invoker,
            wandData.invariable,
            wandData.highPayload.cypherArray,
            helperBundle,
            getInvokePosDire(level, invoker, wandData.invariable.chunkF.wandLength),
        )
        helper.start()
        helper.finalizeInvoking()
        instance.updateHelperData(helperBundle) // retrieve data from helper and sync to instance of both sides

        return true
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