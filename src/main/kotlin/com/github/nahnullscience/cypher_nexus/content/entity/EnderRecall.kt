package com.github.nahnullscience.cypher_nexus.content.entity

import com.github.nahnullscience.cypher_nexus.init.mod.Cyphers.ENDER_RECALL
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractCypherProjectile
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
        if (!level().isClientSide) {
//            val pos = projectile.position()
//            val teleportation = AbstractCypherProjectile.from(level, EnderTeleportationCypher, projectile.owner, )
//            teleportation.setPos(pos)
//            teleportation.existing = 100 // recall after 5seconds, at most
//            level.addFreshEntity(teleportation)
            if (firstTick) {

            }
        }
    }

}