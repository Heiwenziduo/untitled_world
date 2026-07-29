package com.github.nahnullscience.cypher_nexus.mechanic.entity

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.ModDataAttachments
import com.github.nahnullscience.cypher_nexus.utility.PlaneDefinition
import com.github.nahnullscience.cypher_nexus.utility.plus
import net.minecraft.world.entity.Entity
import net.neoforged.bus.api.EventPriority
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.tick.EntityTickEvent

/**
 * can be found through [Entity.getData] on entities that can invoke cyphers. exists on both sides.
 * this class is used to store and update data used by `cypher-entities`.
 * */
class InvokerStateTracker {
    private lateinit var owner: Entity
    private lateinit var viewPlane: PlaneDefinition

    fun tick(entity: Entity) {
        owner = entity

        val looking = entity.headLookAngle
        viewPlane = PlaneDefinition(
            looking,
            looking + entity.eyePosition
            )
    }

    fun getViewPlane(): PlaneDefinition? {
        return if (::viewPlane.isInitialized) viewPlane else null
    }

    @EventBusSubscriber(modid = CypherNexus.MOD_ID)
    companion object {
        @SubscribeEvent(priority = EventPriority.NORMAL)
        private fun updateTracker(event: EntityTickEvent.Post) {
            val entity = event.entity
            if (entity.hasData(ModDataAttachments.INVOKER_STATE_TRACKER)) {
                entity.getData(ModDataAttachments.INVOKER_STATE_TRACKER).tick(entity)
            }
        }
    }
}