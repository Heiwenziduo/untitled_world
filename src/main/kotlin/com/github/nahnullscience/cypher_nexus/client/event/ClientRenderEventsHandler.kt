package com.github.nahnullscience.cypher_nexus.client.event

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.ModDataComponents.WAND_HIGH_PAYLOAD
import com.github.nahnullscience.cypher_nexus.init.ModDataComponents.WAND_INVARIABLE
import net.minecraft.client.Minecraft
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.component.TooltipDisplay
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.EventPriority
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent

@EventBusSubscriber(modid = CypherNexus.MOD_ID, value = [Dist.CLIENT])
object ClientRenderEventsHandler {
    private val mc get() = Minecraft.getInstance()

    @SubscribeEvent(priority = EventPriority.LOW)
    private fun renderWandTooltip(e: ItemTooltipEvent) {
        val stack = e.itemStack
        val list = e.toolTip
        val context = e.context
        val flag = e.flags
        val display = stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT)
        stack.addToTooltip(WAND_INVARIABLE, context, display, list::add, flag)
        stack.addToTooltip(WAND_HIGH_PAYLOAD, context, display, list::add, flag)
    }
}