package com.github.nahnullscience.cypher_nexus.content.cypher.projectile

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.ModEntities
import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractCypherProjectile
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileNode
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileStateChunk
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.TriggerType
import net.minecraft.core.particles.ItemParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level

object SpawnEggCypher : ProjectileCypher() {
    override val resource = CypherNexus.modResource("spawn_egg")
    override val projectileType = ModEntities.CYPHER_SPAWN_EGG

//    val egg = ItemStack(Items.EGG)

    override fun defaultAttributes(): CypherDataMap.Builder {
        return super.defaultAttributes()
            .manaDrain(20f)
            .draw(1)
            .flags(CypherFlags.STICKY)
            .projectileAttr(CypherAttributes.SPEED, 0.8)
            .projectileAttr(CypherAttributes.EXISTING, 300.0)
            .projectileAttr(CypherAttributes.GRAVITY_FACTOR, 0.03)
    }

//    override fun visualEffectOnHit(level: Level, projectile: AbstractCypherProjectile) {
//        val pos = projectile.position()
//        for (i in 0..7) {
//            level.addParticle(ItemParticleOption(ParticleTypes.ITEM, egg), pos.x, pos.y, pos.z, 0.0, 0.0, 0.0)
//        }
//    }

    override fun addToStateChunk(chunk: ProjectileStateChunk): ProjectileStateChunk {
        val subState = ProjectileStateChunk()
        chunk.addProjectile(ProjectileNode(this, subState, TriggerType.COLLISION))
        return subState
    }
}