package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.steerer.AbstractCypherSteerer
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileNode
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotState
import net.minecraft.core.Holder
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntitySpawnReason
import net.minecraft.world.phys.Vec3

/**
 * generate projectile with attributes initialized
 * @see net.minecraft.world.level.entity.PersistentEntitySectionManager.addEntity
 * */
fun <CE> AbstractProjectileCypher<CE>.spawnCypherEntity(
    level: ServerLevel,
    shotState: ShotState,
    invoker: Entity?,
    node: ProjectileNode?,
    position: Vec3,
    direction: Vec3
) where CE : Entity, CE : ICypherEntity {
    val proj = createCypherEntity(this, level)
    proj.setOwner(invoker)
    proj.initCypher(this, shotState, node, null)
    proj.initPositionDirection(position, direction)
    proj.initEntity(proj)
    level.addFreshEntity(proj)
}

fun Holder<out AbstractProjectileCypher<*>>.spawnCypherEntityRaw(
    level: ServerLevel,
    steerer: Holder<out AbstractCypherSteerer>,
    owner: Entity? = null,
    position: Vec3,
    direction: Vec3
) = this.value().spawnCypherEntityRaw(level, steerer, owner, position, direction)
fun <CE> AbstractProjectileCypher<CE>.spawnCypherEntityRaw(
    level: ServerLevel,
    steerer: Holder<out AbstractCypherSteerer>,
    owner: Entity? = null,
    position: Vec3,
    direction: Vec3
) where CE : Entity, CE : ICypherEntity {
    val proj = createCypherEntity(this, level)
    proj.setOwner(owner)
    proj.initCypher(this, null, steerer.value())
    proj.initPositionDirection(position, direction)
    proj.initEntity(proj)
    level.addFreshEntity(proj)
}

private fun <CE> createCypherEntity(
    cypher: AbstractProjectileCypher<CE>,
    level: ServerLevel,
) : CE where CE : Entity, CE : ICypherEntity {
    val entityType = cypher.projectileType.get()
    val proj = entityType.create(level, EntitySpawnReason.SPAWN_ITEM_USE) ?:
    throw IllegalStateException("Failed to create projectile [$entityType].")
    return proj
}
