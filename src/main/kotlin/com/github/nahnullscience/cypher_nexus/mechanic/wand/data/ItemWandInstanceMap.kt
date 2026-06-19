package com.github.nahnullscience.cypher_nexus.mechanic.wand.data

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.ModDataComponents
import com.github.nahnullscience.cypher_nexus.mechanic.wand.IWandLike
import com.github.nahnullscience.cypher_nexus.mechanic.wand.WandDataBundle
import com.github.nahnullscience.cypher_nexus.utility.mod.ArrayOfCyphers
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

class ItemWandInstanceMap {
    companion object {
        const val RESET_TICK_COUNT = 1200
        private fun side(level: Level) = if (level.isClientSide) "client" else "server"
    }

    private val _map = HashMap<String, ItemWandInstance>()


    /** do some garbage collection and free memory */
    fun tick(entity: Entity) {
        if (entity.tickCount % RESET_TICK_COUNT != 0) return
        CypherNexus.debugWand { "${side(entity.level())} side wand data map GC: $entity" }
        _map.entries.removeIf { (uu, instance) ->
            (entity.level().gameTime - instance.lastModifyTime >= RESET_TICK_COUNT)
                .also { if (it) CypherNexus.LOGGER.debug("remove {}", instance) }
        }
    }

    operator fun get(uuid: String): ItemWandInstance? = _map[uuid]

    fun getOrPutInstance(invariable: WandDataInvariable, cypherArray: ArrayOfCyphers, wand: IWandLike?, level: Level): ItemWandInstance {
        val uuid = invariable.uuid
        if (_map[uuid] == null) {
            CypherNexus.debugWand { "wand-like [$wand] just created a ${side(level)} sided instance: [$uuid]" }
        }
        return _map.getOrPut(uuid)
        { ItemWandInstance(invariable, level.isClientSide, cypherArray, this) }
    }
    fun getOrPutInstance(bundle: WandDataBundle, wand: IWandLike?, level: Level) =
        getOrPutInstance(bundle.invariable, bundle.highPayload.cypherArray, wand, level)
//    fun getOrPutInstance(stack: ItemStack, wand: IWandLike, level: Level): WandInstance? {
//        val invariable = stack.get(ModDataComponents.WAND_INVARIABLE) ?: return null
//        val highPayload = stack.get(ModDataComponents.WAND_HIGH_PAYLOAD) ?: return null
//        return getOrPutInstance(WandDataBundle(invariable, highPayload), wand, level)
//    }


    /** main scene is to edit cyphers */
    fun updateWandStats(bundle: WandDataBundle, wand: IWandLike, level: Level) = run {
        if (level.isClientSide) return@run
        getOrPutInstance(bundle, wand, level).updateWandStatsServer(bundle)
    }
    fun updateWandStats(stack: ItemStack,  wand: IWandLike, level: Level) {
        val invariable = stack.get(ModDataComponents.WAND_INVARIABLE) ?: return
        val highPayload = stack.get(ModDataComponents.WAND_HIGH_PAYLOAD) ?: return
        return updateWandStats(WandDataBundle(invariable, highPayload), wand, level)
    }
}