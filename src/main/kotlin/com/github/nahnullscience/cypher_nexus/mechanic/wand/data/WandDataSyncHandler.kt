package com.github.nahnullscience.cypher_nexus.mechanic.wand.data

import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.server.level.ServerPlayer
import net.neoforged.neoforge.attachment.AttachmentSyncHandler
import net.neoforged.neoforge.attachment.IAttachmentHolder

// call AttachmentHolder#syncData(AttachmentType) to sync data manually
// additionally data will also sync when #getData / #setData / #removeData is called
// However both cases are not desired, since we wrapped instances into a map,
// and there seems no way to avoid sync the entire map
// so just disable default sync entirely
/** default sync is disabled */
object WandDataSyncHandler : AttachmentSyncHandler<ItemWandInstanceMap> {
    override fun write(
        buf: RegistryFriendlyByteBuf,
        attachment: ItemWandInstanceMap,
        initialSync: Boolean
    ) = Unit

    override fun read(
        holder: IAttachmentHolder,
        buf: RegistryFriendlyByteBuf,
        previousValue: ItemWandInstanceMap?
    ): ItemWandInstanceMap? = null

    override fun sendToPlayer(holder: IAttachmentHolder, to: ServerPlayer) = false
}