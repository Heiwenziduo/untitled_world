package com.github.nahnullscience.cypher_nexus.utility

import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.projectile.ProjectileUtil
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.Level
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import java.util.function.Predicate

object RayCastUtility {
    /**
     * custom projectile hit check function exactly same as {net.minecraft.world.entity.projectile.ProjectileUtil.getHitResult()},
     * but avoid magic number "0.3" (e.g. margin)
     * */
    fun getProjectileHitResult(start: Vec3, projectile: Entity, filter: Predicate<Entity>, deltaMovement: Vec3, level: Level, margin: Float, clipContext: ClipContext.Block = ClipContext.Block.COLLIDER) : HitResult {
        var end = start.add(deltaMovement)
        var hitresult: HitResult = level.clip(ClipContext(start, end, clipContext, ClipContext.Fluid.NONE, projectile))
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
}