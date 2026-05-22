package com.github.nahnullscience.cypher_nexus.content.cypher.projectile

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.content.entity.AbstractCypherProjectile
import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.HookHitEntityServer
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileNode
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileStateBlock
import net.minecraft.core.particles.ItemParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.phys.HitResult

object SpawnEggCypher : ProjectileCypher(
    manaDrain = 20f
), HookHitEntityServer {
    override val resource = CypherNexus.modResource("spawn_egg")
    val egg = ItemStack(Items.EGG)
    init {
        addFlag(CypherFlags.STICKY)
        addAttribute(CypherAttributes.SPEED, 0.8)
        addAttribute(CypherAttributes.EXISTING, 300.0)
        addAttribute(CypherAttributes.GRAVITY_FACTOR, 0.03)
    }

    override fun visualEffectOnHit(level: Level, projectile: AbstractCypherProjectile) {
        val pos = projectile.position()
        for (i in 0..7) {
            level.addParticle(ItemParticleOption(ParticleTypes.ITEM, egg), pos.x, pos.y, pos.z, 0.0, 0.0, 0.0)
        }
    }

    override fun onHitServer(level: Level, projectile: AbstractCypherProjectile, strength: Int, result: HitResult) {
        projectile.releasePayload()
    }

    override val draw = 1
    override fun addToState(helper: InvokingHelper, state: ProjectileStateBlock): ProjectileStateBlock {
        val subState = ProjectileStateBlock()
        state.addProjectile(ProjectileNode(this, subState))
        return subState
    }
}