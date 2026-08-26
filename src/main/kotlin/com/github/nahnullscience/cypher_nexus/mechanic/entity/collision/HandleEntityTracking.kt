package com.github.nahnullscience.cypher_nexus.mechanic.entity.collision

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.ModDataAttachments
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractDedicatedCypherProjectile
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.vehicle.VehicleEntity
import net.neoforged.bus.api.EventPriority
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.entity.EntityEvent.EnteringSection
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent.StartTracking
import net.neoforged.neoforge.event.tick.EntityTickEvent
import net.neoforged.neoforge.event.tick.LevelTickEvent

@EventBusSubscriber(modid = CypherNexus.MOD_ID)
object HandleEntityTracking {

    /**
     * check [net.minecraft.server.level.ServerEntity.addPairing]
     * */
    private fun trackingCypherProjectiles(e: StartTracking) {

    }

    private fun enterSection(e: EnteringSection) {

    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    private fun updateTracking(e: EntityTickEvent.Post) {
        val entity = e.entity
        val manager = entity.level().getData(ModDataAttachments.STORAGE_GRID_MANAGER)
        if (entity is ItemEntity) {
            manager.updateItem(entity)
            return
        }
        if (entity.needTrackingInGrid()) {
            manager.updateEntity(entity)
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    private fun stopTracking(e: EntityLeaveLevelEvent) {
        val entity = e.entity
        val manager = entity.level().getData(ModDataAttachments.STORAGE_GRID_MANAGER)
        manager.removeEntity(entity)
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    private fun updateStorageManager(e: LevelTickEvent.Post) {
        val manager = e.level.getData(ModDataAttachments.STORAGE_GRID_MANAGER)
        manager.levelTick()
    }

    /**
     *
     * */
    fun Entity.needTrackingInGrid(): Boolean {
        return  if   (this is AbstractDedicatedCypherProjectile) enableGridTrack()
                else (this is LivingEntity) ||
                     (this is VehicleEntity && this.canBeHitByProjectile())
    }
}
