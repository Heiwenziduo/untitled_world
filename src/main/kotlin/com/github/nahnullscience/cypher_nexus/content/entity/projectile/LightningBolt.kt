package com.github.nahnullscience.cypher_nexus.content.entity.projectile

import com.github.nahnullscience.cypher_nexus.init.mod.Cyphers
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.AbstractDedicatedCypherProjectile
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.Level

class LightningBolt(
    entityType: EntityType<out AbstractDedicatedCypherProjectile>,
    level: Level
) : AbstractDedicatedCypherProjectile(entityType, level) {
    override val cypherHolder = Cyphers.LIGHTNING_BOLT
    var seed: Long = random.nextLong()
        private set

    override fun tick() {
        super.tick()
        seed = random.nextLong()
    }

    override fun shouldRender(x: Double, y: Double, z: Double): Boolean = true // doesn't affect by distance
}