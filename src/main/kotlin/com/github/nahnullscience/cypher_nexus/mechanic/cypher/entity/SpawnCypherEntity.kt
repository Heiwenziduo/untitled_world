package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.steerer.AbstractCypherSteerer
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileNode
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk
import com.github.nahnullscience.cypher_nexus.utility.PosDirePair
import net.minecraft.core.Holder
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntitySpawnReason

fun <CE> AbstractProjectileCypher<CE>.spawnCypherEntity(
    level: ServerLevel,
    invoker: Entity?,
    shotState: ShotStateChunk,
    node: ProjectileNode?,
    posDire: PosDirePair,
) where CE : Entity, CE : ICypherEntity {
    val proj = createCypherEntity(this, level, invoker, shotState, node)
    proj.initDirection(posDire)
    level.addFreshEntity(proj)
}

/** generate projectile with attributes initialized */
fun <CE> createCypherEntity(
    cypher: AbstractProjectileCypher<CE>,
    level: ServerLevel,
    invoker: Entity?,
    shotState: ShotStateChunk,
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
    posDire: PosDirePair? = null,
) {
    fun <CE> spawn(cypher: AbstractProjectileCypher<CE>) where CE : Entity, CE : ICypherEntity {
        val proj = createCypherEntityRaw(cypher, level, steerer.value(), owner)
        posDire?.let { proj.initDirection(it) }
        level.addFreshEntity(proj)
    }
    spawn(cypher.value())
}

