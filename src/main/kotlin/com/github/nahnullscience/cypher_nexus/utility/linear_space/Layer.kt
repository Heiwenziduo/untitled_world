package com.github.nahnullscience.cypher_nexus.utility.linear_space

import com.github.nahnullscience.cypher_nexus.utility.randomPerpendicularNormalD
import com.github.nahnullscience.cypher_nexus.utility.set
import com.github.nahnullscience.cypher_nexus.utility.toV3d
import net.minecraft.core.Direction
import net.minecraft.core.Direction.Axis
import net.minecraft.util.RandomSource
import net.minecraft.world.phys.Vec3
import org.joml.Vector3d
import org.joml.Vector3dc

/*
 * communicate with minecraft world
 * */
/**
 * require front & left are unified and perpendicular to each other
 * */
fun AnchoredCoordinate.Companion.fromFrontLeftOrthonormal(front: Vec3, left: Vec3): AnchoredCoordinate {
    return fromFrontLeftOrthonormal(front.toV3d(), left.toV3d())
}

fun AnchoredCoordinate.Companion.fromDirectionWithUpVector(
    direction: Direction,
    approximateUp: Vector3d,
    fallback: () -> Vector3d
): AnchoredCoordinate {
    val front = Vector3d().set(direction.unitVec3)
    when(direction.axis) {
        Axis.X -> approximateUp.x = 0.0
        Axis.Y -> approximateUp.y = 0.0
        Axis.Z -> approximateUp.z = 0.0
    }
    val up =
        if (approximateUp.lengthSquared() > 1e-6) approximateUp.normalize()
        else fallback() // if speed vector and direction are in the same direction

    return fromFrontUpOrthonormal(front, up)
}

fun AnchoredCoordinate.Companion.fromDirectionWithUpVector(
    direction: Direction,
    approximateUp: Vector3d,
    random: RandomSource
) = fromDirectionWithUpVector(direction, approximateUp) { direction.axis.randomPerpendicularNormalD(random) }


fun AnchoredCoordinate.anchor(v: Vec3) = anchor(v.x, v.y, v.z)
fun AnchoredCoordinate.anchor(v: Vector3dc) = anchor(v.x(), v.y(), v.z())
fun AnchoredCoordinate.face(v: Vec3) = face(v.x, v.y, v.z)
/**
 * Rigidly reorients the coordinate frame so `front` faces [targetDir].
 * Preserves the relative roll and perpendicularity of `left` and `up`.
 */
fun AnchoredCoordinate.face(targetDir: Vector3dc) = face(targetDir.x(), targetDir.y(), targetDir.z())
fun AnchoredCoordinate.putCache(index: Int, pos: Vector3dc, dir: Vector3dc) = putCache(index, pos.x(), pos.y(), pos.z(), dir.x(), dir.y(), dir.z())
fun AnchoredCoordinate.putCache(index: Int, pos: Vec3, dir: Vec3) = putCache(index, pos.x(), pos.y(), pos.z(), dir.x(), dir.y(), dir.z())
