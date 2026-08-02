package com.github.nahnullscience.cypher_nexus.init

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.utility.exception.VanillaMisuseException
import net.minecraft.core.Registry
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent

/* @doc lifecycle:
 * mod constructor(FMLConstructModEvent) ->
 * registries(NewRegistryEvent, DataPackRegistryEvent.NewRegistry, RegisterEvent) ->
 * CommonSetup ->
 * sided setup(FMLClientSetupEvent, FMLDedicatedServerSetupEvent)
 * */
@EventBusSubscriber(modid = CypherNexus.MOD_ID)
object LifeCycle {

    @SubscribeEvent
    private fun commonSetup(event: FMLCommonSetupEvent) {
        // this is a parallel dispatched event - we cannot interact with game state in this event.
        CypherNexus.LOGGER.info("HELLO FROM COMMON SETUP")

//        if (Config.logDirtBlock) CypherNexus.LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT))
//        CypherNexus.LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber)

        // Config.items.forEach(Consumer { item: Item? -> LOGGER.info("ITEM >> {}", item.toString()) })

        // CypherData.Companion.init()
    }

    fun <T : Any> Registry<T>.getIdOfBound(item: T, bounds: Int = Int.MAX_VALUE): Int {
        return getId(item).also {
            if (it > bounds) throw VanillaMisuseException("[${this}] registry id-$it is out of bound $bounds")
        }
    }
}