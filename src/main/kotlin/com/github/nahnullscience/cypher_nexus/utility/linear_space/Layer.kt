package com.github.nahnullscience.cypher_nexus.utility.linear_space

import com.github.nahnullscience.cypher_nexus.utility.toV3d
import net.minecraft.world.phys.Vec3
import org.joml.Vector3dc

/*
 * communicate with minecraft world
 * */
fun AnchoredCoordinate.Companion.fromFrontLeft(front: Vec3, left: Vec3): AnchoredCoordinate {
    return fromFrontLeft(front.toV3d(), left.toV3d())
}

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
