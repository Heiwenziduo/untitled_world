package com.github.nahnullscience.cypher_nexus.mechanic.wand.data

import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.server.level.ServerPlayer
import net.neoforged.neoforge.attachment.AttachmentSyncHandler
import net.neoforged.neoforge.attachment.IAttachmentHolder

// call AttachmentHolder#syncData(AttachmentType) to sync data manually
// otherwise data will only sync when getData / setData / removeData is called
// However both cases are not desired, since we wrapped instances into a map,
// and there seems no way to avoid sync the entire map
object WandDataSyncHandler : AttachmentSyncHandler<WandInstanceMap> {
    override fun write(
        buf: RegistryFriendlyByteBuf,
        attachment: WandInstanceMap,
        initialSync: Boolean
    ) {
    }

    override fun read(
        holder: IAttachmentHolder,
        buf: RegistryFriendlyByteBuf,
        previousValue: WandInstanceMap?
    ): WandInstanceMap? {
        TODO("Not yet implemented")
    }

    // only update the player self-data
    override fun sendToPlayer(holder: IAttachmentHolder, to: ServerPlayer) = holder == to
}