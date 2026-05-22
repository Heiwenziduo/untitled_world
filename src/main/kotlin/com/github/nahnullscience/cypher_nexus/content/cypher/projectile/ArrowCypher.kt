package com.github.nahnullscience.cypher_nexus.content.cypher.projectile

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.content.entity.AbstractCypherProjectile
import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ProjectileCypher
import net.minecraft.core.particles.ItemParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level

object ArrowCypher : ProjectileCypher(
    manaDrain = 10f
) {
    override val resource = CypherNexus.modResource("arrow")
    val stack = ItemStack(Items.ARROW)

    init {
        addAttribute(CypherAttributes.CAST_DELAY, 1.0)
        addAttribute(CypherAttributes.RECHARGE_TIME, 1.0)

        addAttribute(CypherAttributes.DAMAGE, 3.0)
        addAttribute(CypherAttributes.SPEED, 1.0)
        addAttribute(CypherAttributes.EXISTING, 300.0)
        addAttribute(CypherAttributes.GRAVITY_FACTOR, 0.01)
    }

    override fun visualEffectOnHit(level: Level, projectile: AbstractCypherProjectile) {
        // check: ItemParticleOption(ParticleTypes.ITEM, itemstack), and ParticleTypes.ITEM_SNOWBALL
        val pos = projectile.position()
        for (i in 0..7) {
            level.addParticle(ItemParticleOption(ParticleTypes.ITEM, stack),
                pos.x, pos.y, pos.z, 0.0, 0.0, 0.0)
        }
    }
}