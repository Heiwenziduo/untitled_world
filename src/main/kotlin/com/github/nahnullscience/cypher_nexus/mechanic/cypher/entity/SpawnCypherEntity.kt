package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity

import com.github.nahnullscience.cypher_nexus.init.mod.CypherSteerers.NO_STEERER
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.steerer.AbstractCypherSteerer
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileNode
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk
import net.minecraft.core.Holder
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntitySpawnReason
import net.minecraft.world.entity.EntityType
import java.util.function.Supplier

fun <CE> AbstractProjectileCypher<CE>.createProjectile(
    level: ServerLevel,
    invoker: Entity?,
    shotState: ShotStateChunk,
    node: ProjectileNode?,
): CE where CE : Entity, CE : ICypherEntity {
    val proj = createCypherEntity(
        this,
        projectileType.get(),
        level,
        invoker,
        shotState,
        node,
    )
    return proj
}

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
fun <CE> createCypherEntityRaw(
    cypher: Holder<out AbstractProjectileCypher<CE>>,
    level: ServerLevel,
    steerer: Holder<out AbstractCypherSteerer> = NO_STEERER,
    owner: Entity? = null,
) : CE where CE : Entity, CE : ICypherEntity =
    createCypherEntityRaw(cypher.value(), level, steerer.value(), owner)
