package com.github.nahnullscience.cypher_nexus

import com.github.nahnullscience.cypher_nexus.init.*
import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.init.mod.CypherBehaviorHooks
import com.github.nahnullscience.cypher_nexus.init.mod.CypherCategories
import com.github.nahnullscience.cypher_nexus.init.mod.Cyphers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.Identifier
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.event.server.ServerStartingEvent
import net.neoforged.neoforge.registries.NewRegistryEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlin.time.Duration.Companion.seconds


/* @doc
 * An entry in neoforge.mods.toml does not need a corresponding @Mod annotation.
 * Likewise, an entry in the neoforge.mods.toml can have multiple @Mod annotations,
 * for example, if you want to separate common logic and client-only logic.
 * */
@Mod(CypherNexus.MOD_ID)
@EventBusSubscriber(modid = CypherNexus.MOD_ID)
object CypherNexus {
    const val MOD_ID: String = "cypher_nexus"

    val LOGGER: Logger = LogManager.getLogger(MOD_ID)
    val LOGGER_CYPHER: Logger = LogManager.getLogger("${MOD_ID}_cyphers")

    inline fun debugCypher(supplier: () -> String) {
        if (false)
        LOGGER.debug(supplier.invoke())
    }
    inline fun debug(supplier: () -> String) {
        LOGGER.debug(supplier.invoke())
    }
    inline fun info(supplier: () -> String) {
        LOGGER.info(supplier.invoke())
    }
    inline fun warn(supplier: () -> String) {
        LOGGER.warn(supplier.invoke())
    }

    fun modResource(path: String) = Identifier.fromNamespaceAndPath(MOD_ID, path)

    fun modTranslation(namespace: String, path: String = ""): MutableComponent =
        Component.translatable("$namespace.$MOD_ID${if (!path.isEmpty()) ".$path" else ""}")

    init {
        LOGGER.info("Hello world!")

        ModBlocks.register()
        ModItems.register()
        ModEntities.register()
        ModDataComponents.register()
        ModDataSerializers.register()
        ModDataLootFunctions.register()
        ModDataAttachments.register()

        ModTabs.register()

        Cyphers.register()
        CypherAttributes.register()
        CypherCategories.register()
        CypherBehaviorHooks.register()

//        // Kotlin style events register
//        val obj = runForDist(
//            clientTarget = {
//                MOD_BUS.addListener(::onClientSetup)
//                Minecraft.getInstance()
//            },
//            serverTarget = {
//                MOD_BUS.addListener(::onServerSetup)
//                "test"
//            })
//
//        println(obj) // Minecraft

        CoroutineScope(Dispatchers.Default).launch {
//            LOGGER.log(Level.INFO, "Before delay")
            delay(5.seconds)
//            LOGGER.log(Level.INFO, "After 5 seconds")
        }

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class () to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        // NeoForge.EVENT_BUS.register(this)
    }
//    /**
//     * This is used for initializing client specific
//     * things such as renderers and keymaps
//     * Fired on the mod specific event bus.
//     */
//    private fun onClientSetup(event: FMLClientSetupEvent) {
//        LOGGER.log(Level.INFO, "HELLO, Initializing client...")
//    }

//    /**
//     * Fired on the global Forge bus.
//     */
//    private fun onServerSetup(event: FMLDedicatedServerSetupEvent) {
//        LOGGER.log(Level.INFO, "HELLO, Server starting...")
//    }

    @SubscribeEvent
    fun registerRegistries(event: NewRegistryEvent) {
        event.register(Cyphers.REGISTRY)
        event.register(CypherAttributes.REGISTRY)
        event.register(CypherCategories.REGISTRY)
        event.register(CypherBehaviorHooks.REGISTRY)
    }

    @SubscribeEvent
    private fun onServerStarting(event: ServerStartingEvent) {
        // LOGGER.info("HELLO from server starting")
    }
}