package com.github.nahnullscience.cypher_nexus.content.entity

import com.github.nahnullscience.cypher_nexus.init.ModEntities.CYPHER_ENDER_TELEPORTATION
import com.github.nahnullscience.cypher_nexus.init.mod.Cyphers.ENDER_RECALL
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractCypherProjectile
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.projectile.ItemSupplier
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level

class EnderRecall(
    entityType: EntityType<out AbstractCypherProjectile>,
    level: Level
) : EnderTeleportation(entityType, level), ItemSupplier {
    override val cypherHolder = ENDER_RECALL
    override fun getItem() = ItemStack(Items.ENDER_PEARL)

    override fun onTickBeforeBoth() {
        if (level() is ServerLevel) {
            if (firstTick) {
                val teleport = createRaw(CYPHER_ENDER_TELEPORTATION.get(), level() as ServerLevel)
                teleport.owner = getOwner()
                teleport.setPos(position())
                teleport.existing = 100
                level().addFreshEntity(teleport)
            }
        }
    }

}