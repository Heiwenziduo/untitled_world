package com.github.nahnullscience.cypher_nexus.content.entity

import com.github.nahnullscience.cypher_nexus.init.ModEntities.CYPHER_ENDER_TELEPORTATION
import com.github.nahnullscience.cypher_nexus.init.mod.Cyphers.ENDER_RECALL
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractDedicatedCypherProjectile
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.projectile.ItemSupplier
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level

class EnderRecall(
    entityType: EntityType<out AbstractDedicatedCypherProjectile>,
    level: Level
) : EnderTeleportation(entityType, level), ItemSupplier {
    override val cypherHolder = ENDER_RECALL
    override fun getItem() = ItemStack(Items.ENDER_PEARL)

    override fun onFirstTick() {
        if (level() is ServerLevel) {
            val teleport = createRaw(CYPHER_ENDER_TELEPORTATION.get(), level() as ServerLevel, owner())
            teleport.setPos(position())
            teleport.setExisting(85 + getExisting())
            level().addFreshEntity(teleport)
        }
        super.onFirstTick()
    }

}