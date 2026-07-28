package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileNode
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntitySpawnReason
import net.minecraft.world.entity.EntityType
import java.util.function.Supplier

/** generate projectile with attributes initialized */
fun <CE> createCypherEntity(
    cypher: AbstractProjectileCypher<*>,
    entityType: EntityType<CE>,
    level: ServerLevel,
    invoker: Entity?,
    shotState: ShotStateChunk,
    node: ProjectileNode?,
) : CE where CE : Entity, CE : ICypherEntity {
    val proj = entityType.create(level, EntitySpawnReason.SPAWN_ITEM_USE) ?:
    throw IllegalStateException("Failed to create projectile [$entityType].")
    proj.setOwner(invoker)
    proj.initCypher(cypher, shotState, node)
    return proj
}

fun <CE> createCypherEntityRaw(
    entityType: EntityType<CE>,
    level: ServerLevel,
    owner: Entity?
) : CE where CE : Entity, CE : ICypherEntity {
    val proj = entityType.create(level, EntitySpawnReason.SPAWN_ITEM_USE) ?:
    throw IllegalStateException("Failed to create projectile [$entityType].")
    proj.setOwner(owner)
    return proj
}
fun <CE> createCypherEntityRaw(
    type: Supplier<EntityType<CE>>,
    level: ServerLevel,
    owner: Entity?
) : CE where CE : Entity, CE : ICypherEntity = createCypherEntityRaw(type.get(), level, owner)

fun <CE> AbstractProjectileCypher<CE>.createProjectile(
    level: ServerLevel,
    invoker: Entity?,
    shootState: ShotStateChunk,
    node: ProjectileNode?,
): CE where CE : Entity, CE : ICypherEntity {
    val proj = createCypherEntity(
        this,
        projectileType.get(),
        level,
        invoker,
        shootState,
        node,
    )
    return proj
}