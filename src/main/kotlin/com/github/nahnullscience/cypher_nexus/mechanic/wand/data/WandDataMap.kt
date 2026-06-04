package com.github.nahnullscience.cypher_nexus.mechanic.wand.data

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.ModDataComponents
import com.github.nahnullscience.cypher_nexus.mechanic.wand.IWandLike
import com.github.nahnullscience.cypher_nexus.mechanic.wand.WandDataBundle
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

// maybe player only?
class WandDataMap {
    companion object {
        const val RESET_TICK_COUNT = 1200
    }

    private val _map = HashMap<String, WandDataInstance>()

    /** do some garbage collection and free memory */
    fun tick(entity: Entity) {
        if (entity.tickCount % RESET_TICK_COUNT != 0) return
        CypherNexus.LOGGER.debug("wand data map GC: {}", entity)
        _map.entries.removeIf { (uu, instance) ->
            entity.level().gameTime - instance.lastModifyTime >= RESET_TICK_COUNT
        }
    }

    operator fun get(uuid: String) = _map[uuid]

    fun getOrPutInstance(bundle: WandDataBundle, wand: IWandLike, level: Level): WandDataInstance {
        val uuid = bundle.invariable.uuid
        if (_map[uuid] == null) {
            CypherNexus.LOGGER.debug("wand-like {} just created an {} sided instance", wand, if (level.isClientSide) "client" else "server" )
        }
        return _map.getOrPut(uuid)
        { WandDataInstance(bundle.invariable, level.isClientSide, bundle.highPayload.cypherArray) }
    }
    fun getOrPutInstance(stack: ItemStack, wand: IWandLike, level: Level): WandDataInstance? {
        val invariable = stack.get(ModDataComponents.WAND_INVARIABLE) ?: return null
        val highPayload = stack.get(ModDataComponents.WAND_HIGH_PAYLOAD) ?: return null
        return getOrPutInstance(WandDataBundle(invariable, highPayload), wand, level)
    }


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