package com.github.nahnullscience.cypher_nexus.init

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.mod.Cyphers
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.registries.datamaps.DataMapType
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent

@EventBusSubscriber(modid = CypherNexus.MOD_ID)
object ModDataMaps {
    val CYPHER_DATA_ATTACH: DataMapType<AbstractCypher, CypherDataMap> =
        DataMapType.builder(
            // data/cypher_nexus/data_maps/cypher/cypher_data_attach.json.
            CypherNexus.modResource("cypher_data_attach"),
            Cyphers.RESOURCE_KEY,
            CypherDataMap.CODEC
        ).synced(
            // The codec used for syncing. May be identical to the normal codec, but may also be
            // a codec with less fields, omitting parts of the object that are not required on the client.
            CypherDataMap.CODEC_SYNC,
            // Whether the data map is mandatory or not. Marking a data map as mandatory will disconnect clients
            // that are missing the data map on their side; this includes vanilla clients.
            true
        ).build()

    @SubscribeEvent // on the mod event bus
    fun registerDataMapTypes(event: RegisterDataMapTypesEvent) {
        event.register(CYPHER_DATA_ATTACH)
    }
}