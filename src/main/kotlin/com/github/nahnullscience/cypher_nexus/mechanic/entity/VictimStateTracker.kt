package com.github.nahnullscience.cypher_nexus.mechanic.entity

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.ModDataAttachments
import net.minecraft.world.entity.Entity
import net.neoforged.bus.api.EventPriority
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.tick.EntityTickEvent

/**
 * can be found through [Entity.getData] on entities that can be `hit` by cyphers.
 * only exist on `server` side.
 * */
class VictimStateTracker {
    private var hitFlag: Int = 0

    fun tick(entity: Entity) {
        hitFlag = 0

    }

    enum class HitFlag {

    }

    @EventBusSubscriber(modid = CypherNexus.MOD_ID)
    companion object {
        @SubscribeEvent(priority = EventPriority.NORMAL)
        private fun updateTracker(event: EntityTickEvent.Post) {
            val entity = event.entity
            if (entity.hasData(ModDataAttachments.VICTIM_STATE_TRACKER)) {
                entity.getData(ModDataAttachments.VICTIM_STATE_TRACKER).tick(entity)
            }
        }
    }
}