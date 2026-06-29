package com.github.nahnullscience.cypher_nexus.utility

import net.minecraft.core.Direction
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.projectile.ProjectileUtil
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import java.util.*
import java.util.function.Predicate
import kotlin.jvm.optionals.getOrNull

object RayCastUtility {
    /**
     * custom projectile hit check function exactly same as [net.minecraft.world.entity.projectile.ProjectileUtil.getHitResult],
     * but avoid magic number "0.3" (e.g. margin)
     * */
    fun getProjectileHitResult(
        start: Vec3,
        projectile: Entity,
        filter: Predicate<Entity>,
        deltaMovement: Vec3,
        level: Level,
        margin: Float,
        clipContext: ClipContext.Block = ClipContext.Block.COLLIDER
    ) : HitResult {
        var end = start.add(deltaMovement)
        var hitresult: HitResult = level.clip(
            ClipContext(start, end, clipContext, ClipContext.Fluid.NONE, projectile)
        )
        if (hitresult.type != HitResult.Type.MISS) {
            end = hitresult.getLocation()
        }

        val hitresult1: HitResult? = ProjectileUtil.getEntityHitResult(
            level,
            projectile,
            start,
            end,
            projectile.boundingBox.expandTowards(deltaMovement).inflate(1.0),
            filter,
            margin
        )
        if (hitresult1 != null) {
            hitresult = hitresult1
        }

        return hitresult
    }

//    fun getHitResult(start: Vec3, filter: Predicate<Entity>, deltaMovement: Vec3, level: Level, margin: Float, clipContext: ClipContext.Block = ClipContext.Block.COLLIDER) : HitResult {
//        var end = start.add(deltaMovement)
//        var hitresult: HitResult = level.clip(ClipContext(start, end, clipContext, ClipContext.Fluid.NONE, projectile))
//        if (hitresult.type != HitResult.Type.MISS) {
//            end = hitresult.getLocation()
//        }
//    }

    /**
     * @return the hit point the given line from this to [destination] collide with [bb], null if not collide
     * */
    fun Vec3.rayCast(destination: Vec3, bb: AABB, margin: Double): Vec3? {
        return bb.inflate(margin).clip(this, destination).getOrNull()
    }

    /**
     *
     * */
    inline fun Vec3.rayCastThen(destination: Vec3, bb: AABB, margin: Double, todo: (hitPoint: Vec3, dir: Direction) -> Unit) {
        bb.inflate(margin).clipWithDirection(this, destination, todo)
    }

    /**
     * direct copy from [AABB.clip], but pass a lambda to utilize the direction
     * */
    inline fun AABB.clipWithDirection(from: Vec3, to: Vec3, todo: (hitPoint: Vec3, dir: Direction) -> Unit) = clipWithDirection(minX, minY, minZ, maxX, maxY, maxZ, from, to, todo)
    inline fun AABB.clipWithDirection(
        minX: Double,
        minY: Double,
        minZ: Double,
        maxX: Double,
        maxY: Double,
        maxZ: Double,
        from: Vec3,
        to: Vec3,
        todo: (hitPoint: Vec3, dir: Direction) -> Unit
    ) {
        val scaleReference = doubleArrayOf(1.0)
        val dx: Double = to.x - from.x
        val dy: Double = to.y - from.y
        val dz: Double = to.z - from.z
        val direction = AABB.getDirection(minX, minY, minZ, maxX, maxY, maxZ, from, scaleReference, null, dx, dy, dz)
        if (direction != null) {
            val scale = scaleReference[0]
            todo(from.add(scale * dx, scale * dy, scale * dz), direction)
        }
    }
}