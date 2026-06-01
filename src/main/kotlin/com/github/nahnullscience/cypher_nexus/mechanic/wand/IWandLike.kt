package com.github.nahnullscience.cypher_nexus.mechanic.wand

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.WandDataFrequent
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.WandDataHighPayload
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.WandDataInvariable
import com.github.nahnullscience.cypher_nexus.utility.mod.PosDirePair
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

/**
 * ---casting logics here---
 * Any Item/LivingEntity implemented the interface here should be able to conduct the power of cyphers #tryConduct
 * */
interface IWandLike {

    fun getWandData(stack: ItemStack?, caster: LivingEntity?): WandDataBundle?
    fun setWandData(stack: ItemStack?, invariable: WandDataInvariable?, highPayload: WandDataHighPayload?, frequent: WandDataFrequent)
    fun setWandData(stack: ItemStack?, frequent: WandDataFrequent) = setWandData(stack, null, null, frequent)
    fun setWandData(stack: ItemStack?, bundle: WandDataBundle) = setWandData(stack, bundle.invariable, bundle.highPayload, bundle.frequent)

    /** direction doesn't have to be normalized */
    fun getInvokePosDire(level: Level, invoker: LivingEntity, wandLength: Float): PosDirePair

    /** for item implementations to determine whether it can be modified in cypher-index */
    val isEditableWand: Boolean

//    fun parseCypherList(cyphers: String): List<AbstractCypher> =
//        cyphers.split(",").map {
//            val str = it.split(".")
//            val resource = ResourceLocation.fromNamespaceAndPath(str[0], str[1])
//            ModCyphers.getCypherOrThrow(resource)
//        }


    /**
     * try a manual "draw", may not success due to delay/recharge/disabled/noMana/E.D. e.t.c.
     * */
    fun tryConduct(level: Level, invoker: LivingEntity, stack: ItemStack?): Boolean {
        // TODO let fake-player/machine can cast cyphers
        val wandData = getWandData(stack, invoker)
        if (wandData == null) return false
        val (invariable, highPayload, frequent) = wandData
        if (!validateConduct(frequent)) {
            if (!level.isClientSide) println("casting rejected due to: $frequent")
            return false
        }

        val (cypherList) = highPayload

        if (level.isClientSide){
            // send casting info to server
            return false
        }
        CypherNexus.LOGGER.debug("Casting start, is client side? {}\nCypherList: {}", level.isClientSide, cypherList)
        CypherNexus.LOGGER.debug("read from data component: {}\n\n\n", wandData)


        val bundle = InvokingHelper.HelperDataBundle.of(invariable, frequent)
        val helper = InvokingHelper(
            level, invoker, stack, invariable, cypherList, bundle,
            getInvokePosDire(level, invoker, invariable.chunkF.wandLength),
        )
        helper.start()

        // retrieve data from helper and write to components
        setWandData(stack, bundle.frequentData())

        // auto-sync
        CypherNexus.LOGGER.debug("server write to data component: {}", bundle)
        CypherNexus.LOGGER.debug("Casting finish...")
        return true
    }

    fun validateConduct(frequent: WandDataFrequent): Boolean {
        val disable = frequent.delay > 0 || (frequent.deck == 0L && frequent.recharge > 0)
        return !disable
    }


    data class WandDataBundle(val invariable: WandDataInvariable, val highPayload: WandDataHighPayload, val frequent: WandDataFrequent)
}