package com.github.nahnullscience.cypher_nexus.mechanic

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.ModDataAttachments.WAND_DATA_MAP
import net.neoforged.bus.api.EventPriority
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.event.tick.EntityTickEvent


@EventBusSubscriber(modid = CypherNexus.MOD_ID)
object EventsListener {

    @SubscribeEvent(priority = EventPriority.NORMAL)
    fun tickWandDataMap(tickEvent: EntityTickEvent.Post) {
        // fired on both sides
        if (tickEvent.entity.hasData(WAND_DATA_MAP)) {
            val dataMap = tickEvent.entity.getData(WAND_DATA_MAP)
            dataMap.tick(tickEvent.entity)
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    fun copyWandDataOnDeath(event: PlayerEvent.Clone) {
        if (event.isWasDeath && event.original.hasData(WAND_DATA_MAP)) {
            "o.O"
        }
    }
}