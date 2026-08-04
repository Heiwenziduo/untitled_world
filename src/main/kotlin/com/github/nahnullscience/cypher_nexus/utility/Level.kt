package com.github.nahnullscience.cypher_nexus.utility

import com.github.nahnullscience.cypher_nexus.utility.LevelUtil.CLIENT
import com.github.nahnullscience.cypher_nexus.utility.LevelUtil.SERVER
import net.minecraft.core.Direction
import net.minecraft.util.profiling.Profiler
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.ClipContext.Block
import net.minecraft.world.level.ClipContext.Fluid
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.HitResult.Type
import net.minecraft.world.phys.Vec3

private object LevelUtil {
    const val CLIENT = "client"
    const val SERVER = "server"
}

fun Level.sideString(): String = if (isClientSide) CLIENT else SERVER

inline val Level.isServerSide get() = !isClientSide

inline val Entity.level get() = level()

/**
 * an optimized entity searching function.
 * */
inline fun Level.forEachEntityWithin(
    except: Entity?,
    bb: AABB,
    crossinline selector: (Entity) -> Boolean,
    crossinline action: (Entity) -> Unit
) {
    Profiler.get().incrementCounter { "getEntities" }
    this.entities.get(bb) { entity ->
        if (entity != except && selector(entity)) {
            action(entity)
        }
    }

    for (part in this.dragonParts()) {
        if (part != except &&
            part.parent != except &&
            selector(part) &&
            bb.intersects(part.boundingBox)
        ) {
            action(part)
        }
    }
}

/**
 * perform a [Level.clipIncludingBorder] and [Level.getEntities] to get the closest hit point between [from] and [to].
 * */
fun Level.nearestHitPoint(from: Vec3, to: Vec3, context: Entity, margin: Double): Vec3 {
    var t = to
    val f: processHit = { h, d -> t = h }
    nearestHitPointThen(from, to, context, margin, f)
    return t
}

/**
 * perform a [Level.clipIncludingBorder] and [Level.getEntities] to get the closest hit point between [from] and [to].
 * */
inline fun Level.nearestHitPointThen(from: Vec3, to: Vec3, context: Entity, margin: Double, then: processHit) {
    var hit = false
    var hitDir: Direction = Direction.UP
    var destination = to
    val blockResult = this.clipIncludingBorder(
        ClipContext(from, destination, Block.COLLIDER, Fluid.NONE, context)
    )
    if (blockResult.type != Type.MISS) {
        destination = blockResult.location
        hitDir = blockResult.direction
        hit = true
    }
    var nearest = Double.MAX_VALUE
    this.forEachEntityWithin(
        context,
        AABB(from, to),
        { e -> e.canBeHitByProjectile() }
    ) { target ->
        from.rayCastThen(destination, target.boundingBox, margin) { hitPoint, dir ->
            val dd: Double = from.distanceToSqr(hitPoint)
            if (dd < nearest) {
                nearest = dd
                destination = hitPoint
                hitDir = dir
                hit = true
            }
        }
    }

    if (hit) {
        then(destination, hitDir)
    }
}