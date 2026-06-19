package com.github.nahnullscience.cypher_nexus.mechanic

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.ModDataAttachments.WAND_DATA_MAP
import com.github.nahnullscience.cypher_nexus.mechanic.event.CNEvents
import com.github.nahnullscience.cypher_nexus.mechanic.event.PlayerGatherWandEvent
import com.github.nahnullscience.cypher_nexus.mechanic.wand.IWandLike
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Inventory.SLOT_OFFHAND
import net.neoforged.bus.api.EventPriority
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.entity.player.PlayerEvent.StartTracking
import net.neoforged.neoforge.event.tick.EntityTickEvent
import net.neoforged.neoforge.event.tick.PlayerTickEvent


@EventBusSubscriber(modid = CypherNexus.MOD_ID)
object EventsListener {

    // tick the wand-data-map to perform GC
    @SubscribeEvent(priority = EventPriority.NORMAL)
    private fun tickWandDataMap(tickEvent: EntityTickEvent.Post) {
        // fired on both sides
        if (tickEvent.entity.hasData(WAND_DATA_MAP)) {
            tickEvent.entity.getData(WAND_DATA_MAP).tick(tickEvent.entity)
        }
    }

    // tick player activated wands and perform basic logic, like generating mana
    @SubscribeEvent(priority = EventPriority.NORMAL)
    private fun wandInstanceUpdatePlayer(event: PlayerTickEvent.Post) {
        val player = event.entity
        val map = player.getData(WAND_DATA_MAP)
        val wands = CNEvents.gatherWandsTracking(player).wands()
        wands.forEach { wand ->
//            (wand.item as IWandLike).itemWandInstance(player.level(), player, wand)?.tick(player)
            map.getOrPutInstance(
                (wand.item as IWandLike).getWandData(wand, null) ?: return@forEach,
                (wand.item as IWandLike),
                player.level()
            ).tick(player)
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    private fun gatherWandsTicking(event: PlayerGatherWandEvent.Tracking) {
        val player = event.entity
        // collect wands in hotbar & offhand
        for (i in 0 until 9) {
            val stack = player.inventory.getItem(i)
            if (IWandLike.validItemWand(stack)) event.addWand(stack)
        }
        val offHand = player.inventory.getItem(SLOT_OFFHAND)
        if (IWandLike.validItemWand(offHand)) event.addWand(offHand)
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    private fun gatherWandsActive(event: PlayerGatherWandEvent.Active) {
        val player = event.entity
//        val mainHand = player.inventory.selectedItem
//        val offHand = player.inventory.getItem(SLOT_OFFHAND)
        val mainHand = player.getItemInHand(InteractionHand.MAIN_HAND)
        val offHand = player.getItemInHand(InteractionHand.OFF_HAND)
        if (IWandLike.validItemWand(mainHand)) event.addWand(mainHand)
        if (IWandLike.validItemWand(offHand)) event.addWand(offHand)
    }


    private fun tackingCypherProjectiles(event: StartTracking) {
        // TODO consider availability
        /**
         * check [net.minecraft.server.level.ServerEntity.addPairing]
         * */
    }

//    @SubscribeEvent(priority = EventPriority.NORMAL)
//    private fun copyWandDataOnDeath(event: PlayerEvent.Clone) {
//        if (event.isWasDeath && event.original.hasData(WAND_DATA_MAP)) {
//            "o.O"
//        }
//    }
}