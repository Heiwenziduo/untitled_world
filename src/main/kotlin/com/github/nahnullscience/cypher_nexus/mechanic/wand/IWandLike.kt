package com.github.nahnullscience.cypher_nexus.mechanic.wand

import com.github.nahnullscience.cypher_nexus.init.ModDataAttachments.WAND_DATA_MAP
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper
import com.github.nahnullscience.cypher_nexus.network.client.ClientboundSyncWandInstance
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

    fun getWandData(stack: ItemStack?, caster: Entity?): WandDataBundle?

    /** direction doesn't have to be normalized */
    fun getInvokePosDire(level: Level, invoker: Entity, wandLength: Float): PosDirePair

    /** for item implementations to determine whether it can be modified in cypher-index */
    val isEditableWand: Boolean


    /**
     * try a manual "draw", may not success due to delay/recharge/disabled/noMana/E.D. e.t.c.
     * TODO let fake-player/machine can cast cyphers
     * */
    fun tryConduct(level: Level, invoker: Entity, stack: ItemStack?): Boolean {
        if (level.isClientSide) return false
        val wandData = getWandData(stack, invoker) ?: return false

        // data-attach (create if not present)
        val instance = invoker.getData(WAND_DATA_MAP).getOrPutInstance(wandData, this, level)

        if (!instance.canInvoke()) {
//            println("casting rejected due to: $instance")
            return false
        }

//        CypherNexus.LOGGER.debug("read from data component: {}\n\n\n", wandData)

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

        // retrieve data from helper and sync to instance of both sides
        instance.updateHelperData(helperBundle)
        if (invoker is ServerPlayer) PacketDistributor.sendToPlayer(invoker,
            ClientboundSyncWandInstance(
                wandData.invariable.uuid,
                helperBundle.manaCurrent,
                helperBundle.delay,
                helperBundle.recharge,
                helperBundle.deck
                )
        )

        return true
    }


}