package com.github.nahnullscience.cypher_nexus.content.cypher.projectile

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.content.entity.CypherProjectile
import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import net.minecraft.core.particles.ItemParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level

object SpawnEggCypher : ProjectileCypher(
    manaDrain = 20f
) {
    override val resource = CypherNexus.modResource("spawn_egg")
    val egg = ItemStack(Items.EGG)
    init {
        addFlag(CypherFlags.STICKY)
        addAttribute(CypherAttributes.SPEED, 0.8)
        addAttribute(CypherAttributes.EXISTING, 300.0)
        addAttribute(CypherAttributes.GRAVITY_FACTOR, 0.03)
    }

    override fun visualEffectOnHit(level: Level, projectile: CypherProjectile) {
        val pos = projectile.position()
        for (i in 0..7) {
            level.addParticle(ItemParticleOption(ParticleTypes.ITEM, egg), pos.x, pos.y, pos.z, 0.0, 0.0, 0.0)
        }
    }
}