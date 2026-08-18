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

fun <CE> AbstractProjectileCypher<CE>.spawnCypherEntity(
    level: ServerLevel,
    shotState: ShotState,
    invoker: Entity?,
    node: ProjectileNode?,
    position: Vec3,
    direction: Vec3
) : CE where CE : Entity, CE : ICypherEntity {
    val proj = createCypherEntity(this, level, shotState, invoker, node)
    proj.initPositionDirection(position, direction)
    return proj.also { level.addFreshEntity(it) }
}

/** generate projectile with attributes initialized */
fun <CE> createCypherEntity(
    cypher: AbstractProjectileCypher<CE>,
    level: ServerLevel,
    shotState: ShotState,
    invoker: Entity?,
    node: ProjectileNode?,
) : CE where CE : Entity, CE : ICypherEntity {
    val entityType = cypher.projectileType.get()
    val proj = entityType.create(level, EntitySpawnReason.SPAWN_ITEM_USE) ?:
    throw IllegalStateException("Failed to create projectile [$entityType].")
    proj.setOwner(invoker)
    proj.initCypher(cypher, shotState, node, null)
    return proj
}

fun <CE> createCypherEntityRaw(
    cypher: AbstractProjectileCypher<CE>,
    level: ServerLevel,
    steerer: AbstractCypherSteerer,
    owner: Entity?,
) : CE where CE : Entity, CE : ICypherEntity {
    val entityType = cypher.projectileType.get()
    val proj = entityType.create(level, EntitySpawnReason.SPAWN_ITEM_USE) ?:
    throw IllegalStateException("Failed to create projectile [$entityType].")
    proj.setOwner(owner)
    proj.initCypher(cypher, null, steerer)
    return proj
}

/**
 * spawn from a star-projected cypher holder — the concrete projectile entity type is
 * deliberately unknown to the caller. `spawn<CE>` below is where the star gets captured,
 * ONCE, in isolation, then fully consumed before returning. that containment is the point:
 * every failed attempt tried to relay the captured type back OUT through the caller's own
 * <CE>, forcing two independent type variables to be solved at once through a nested
 * `Holder<out AbstractProjectileCypher<*>>`. keep it local and it's just ordinary generics.
 * */
fun spawnCypherEntityRaw(
    cypher: Holder<out AbstractProjectileCypher<*>>,
    level: ServerLevel,
    steerer: Holder<out AbstractCypherSteerer>,
    owner: Entity? = null,
    position: Vec3,
    direction: Vec3
) {
    fun <CE> spawn(cypher: AbstractProjectileCypher<CE>) where CE : Entity, CE : ICypherEntity {
        val proj = createCypherEntityRaw(cypher, level, steerer.value(), owner)
        proj.initPositionDirection(position, direction)
        level.addFreshEntity(proj)
    }
    spawn(cypher.value())
}

