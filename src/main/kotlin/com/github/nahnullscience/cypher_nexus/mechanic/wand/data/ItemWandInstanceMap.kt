package com.github.nahnullscience.cypher_nexus.mechanic.wand.data

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.ModDataComponents
import com.github.nahnullscience.cypher_nexus.mechanic.wand.IWandLike
import com.github.nahnullscience.cypher_nexus.utility.mod.ArrayOfCyphers
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

/**
 * exist on both sides. this data can be found through [Entity.getData] on any Entity who had used a wand once.
 * */
class ItemWandInstanceMap {
    companion object {
        const val RESET_TICK_COUNT = 1200
        private fun side(level: Level) = if (level.isClientSide) "client" else "server"
    }

    private val _map = HashMap<String, ItemWandInstance>()
    operator fun get(uuid: String): ItemWandInstance? = _map[uuid]


    /**
     * do some garbage collection and free memory
     * */
    fun tick(entity: Entity) {
        if (entity.tickCount % RESET_TICK_COUNT != 0) return
        CypherNexus.debugWand { "${side(entity.level())} side wand data map GC: $entity" }

        _map.entries.removeIf { (uu, instance) ->
            (entity.level().gameTime - instance.lastModifyTime >= RESET_TICK_COUNT)
                .also { if (it) CypherNexus.debugWand { "remove $instance" } }
        }
    }


    fun getOrPutInstance(invariable: WandDataInvariable, aoc: ArrayOfCyphers, wand: IWandLike, level: Level): ItemWandInstance {
        val uuid = invariable.uuid
        return _map.getOrPut(uuid)
        {
            ItemWandInstance(invariable, level.isClientSide, aoc, this, wand)
            .also { CypherNexus.debugWand { "wand-like [$wand] just created a ${side(level)} sided instance: [$uuid]" } }
        }
    }
    fun getOrPutInstance(bundle: WandDataBundle, wand: IWandLike, level: Level) =
        getOrPutInstance(bundle.invariable, bundle.highPayload.aoc, wand, level)



    /** main scene is to edit cyphers */
    fun updateWandStats(bundle: WandDataBundle, wand: IWandLike, level: Level) = run {
        if (level.isClientSide) return@run
        getOrPutInstance(bundle, wand, level).updateWandStatsServer(bundle)
    }
    fun updateWandStats(stack: ItemStack, wand: IWandLike, level: Level) {
        val invariable = stack.get(ModDataComponents.WAND_INVARIABLE) ?: return
        val highPayload = stack.get(ModDataComponents.WAND_HIGH_PAYLOAD) ?: return
        return updateWandStats(WandDataBundle(invariable, highPayload), wand, level)
    }
}