package com.github.nahnullscience.cypher_nexus.mechanic.wand.data

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.ModDataAttachments
import com.github.nahnullscience.cypher_nexus.mechanic.wand.IItemWand
import com.github.nahnullscience.cypher_nexus.utility.mod.ArrayOfCyphers
import com.github.nahnullscience.cypher_nexus.utility.sideString
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.neoforged.bus.api.EventPriority
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.tick.EntityTickEvent
import java.util.*

/**
 * exist on both sides. this data can be found through [Entity.getData] on any Entity who had used a wand once.
 * */
class ItemWandInstanceMap {

    @EventBusSubscriber(modid = CypherNexus.MOD_ID)
    companion object {
        const val RESET_TICK_COUNT = 1200

        /**
         * tick the wand-data-map to perform GC
         * */
        @SubscribeEvent(priority = EventPriority.NORMAL)
        private fun tickWandDataMap(event: EntityTickEvent.Post) {
            // fired on both sides
            val entity = event.entity
            val map = entity.getExistingDataOrNull(ModDataAttachments.WAND_INSTANCE_MAP)
            map?.tick(entity)
        }
    }

    private val uuid2InstanceMap = Object2ReferenceOpenHashMap<UUID, ItemWandInstance>().also { it.defaultReturnValue(null) }
    operator fun get(uuid: UUID): ItemWandInstance? = uuid2InstanceMap[uuid]


    /**
     * do some garbage collection and free memory
     * */
    fun tick(entity: Entity) {
        if (entity.tickCount % RESET_TICK_COUNT != 0) return
        CypherNexus.debugWand { "${entity.level().sideString()} side wand data map GC: $entity" }

        uuid2InstanceMap.entries.removeIf { (uu, instance) ->
            (entity.level().gameTime - instance.lastModifyTime >= RESET_TICK_COUNT).also {
                    if (it) {
                        CypherNexus.debugWand { "remove $instance" }
                        instance.discard()
                    }
                }
        }
    }


    fun getOrPutInstance(level: Level, wandData: ItemWandDataInvariable, aoc: ArrayOfCyphers, wand: IItemWand): ItemWandInstance {
        val uuid = wandData.uuid
        return uuid2InstanceMap.getOrPut(uuid) {
            ItemWandInstance(wandData, wand, level.isClientSide, aoc, this)
            .also { CypherNexus.debugWand { "wand-like [$wand] just created a ${level.sideString()} sided instance: [$uuid]" } }
        }
    }
    fun getOrPutInstance(level: Level, stack: ItemStack, wand: IItemWand): ItemWandInstance {
        val wandData = wand.getWandData(stack)
        val aoc = wand.getInvokingRecipe(stack)
        return getOrPutInstance(level, wandData, aoc, wand)
    }


    /** main scene is to edit cyphers */
    fun updateWandInstance(level: Level, stack: ItemStack, wand: IItemWand) {
        if (level.isClientSide) return
        val wandData = wand.getWandData(stack)
        val aoc = wand.getInvokingRecipe(stack)
        getOrPutInstance(level, wandData, aoc, wand).updateWandStatsServerOnly(wandData, aoc)
    }
}
