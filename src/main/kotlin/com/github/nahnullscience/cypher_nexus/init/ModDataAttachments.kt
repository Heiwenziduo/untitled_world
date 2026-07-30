package com.github.nahnullscience.cypher_nexus.init

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.entity.InvokerStateTracker
import com.github.nahnullscience.cypher_nexus.mechanic.entity.VictimStateTracker
import com.github.nahnullscience.cypher_nexus.mechanic.entity.WandModuleStateTracker
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.WandDataSyncHandler
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.ItemWandInstanceMap
import net.neoforged.neoforge.attachment.AttachmentType
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.NeoForgeRegistries
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS
import java.util.function.Supplier

/** entity data attachments */
object ModDataAttachments {

    val DEFERRED_REGISTER: DeferredRegister<AttachmentType<*>> =
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, CypherNexus.MOD_ID)

    fun register() {
        DEFERRED_REGISTER.register(MOD_BUS)
    }

    val WAND_DATA_MAP: Supplier<AttachmentType<ItemWandInstanceMap>> =
        DEFERRED_REGISTER.register("wand_instance_map") { ->
        // disable sync
        AttachmentType.builder { -> ItemWandInstanceMap() }.sync(WandDataSyncHandler).build()
    }

    val WAND_MODULE_STATE_TRACKER =
        DEFERRED_REGISTER.register("wand_module_state_tracker") { ->
        AttachmentType.builder { -> WandModuleStateTracker() }.build()
    }

    val INVOKER_STATE_TRACKER =
        DEFERRED_REGISTER.register("invoker_state_tracker") { ->
        AttachmentType.builder { -> InvokerStateTracker() }.build()
    }

    val VICTIM_STATE_TRACKER =
        DEFERRED_REGISTER.register("victim_state_tracker") { ->
            AttachmentType.builder { -> VictimStateTracker() }.build()
        }
}