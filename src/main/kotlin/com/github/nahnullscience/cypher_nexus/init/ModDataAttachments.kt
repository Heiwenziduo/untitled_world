package com.github.nahnullscience.cypher_nexus.init

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.entity.InvokerStateTracker
import com.github.nahnullscience.cypher_nexus.mechanic.entity.collision.CETargetStateTracker
import com.github.nahnullscience.cypher_nexus.mechanic.entity.WandModuleStateTracker
import com.github.nahnullscience.cypher_nexus.mechanic.entity.collision.CETargetStorageGridsManager
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.WandDataSyncHandler
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.ItemWandInstanceMap
import net.minecraft.world.level.Level
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

    // Entity
    val WAND_INSTANCE_MAP: Supplier<AttachmentType<ItemWandInstanceMap>> =
        DEFERRED_REGISTER.register("wand_instance_map") { ->
            // disable sync
            AttachmentType.builder { v -> ItemWandInstanceMap() }.sync(WandDataSyncHandler).build()
        }

    val WAND_MODULE_STATE_TRACKER =
        DEFERRED_REGISTER.register("wand_module_state_tracker") { ->
            AttachmentType.builder { v -> WandModuleStateTracker() }.build()
        }

    val INVOKER_STATE_TRACKER =
        DEFERRED_REGISTER.register("invoker_state_tracker") { ->
            AttachmentType.builder { v -> InvokerStateTracker() }.build()
        }

    val CE_TARGET_STATE_TRACKER =
        DEFERRED_REGISTER.register("target_state_tracker") { ->
            AttachmentType.builder { v -> CETargetStateTracker() }.build()
        }

    // Level
    val STORAGE_GRID_MANAGER =
        DEFERRED_REGISTER.register("storage_gird_manager") { ->
            AttachmentType.builder { v -> CETargetStorageGridsManager(v as Level) }.build()
        }
}
