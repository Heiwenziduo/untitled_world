package com.github.nahnullscience.cypher_nexus.utility.linear_space

import com.github.nahnullscience.cypher_nexus.utility.toV3d
import net.minecraft.world.phys.Vec3
import org.joml.Vector3d

/*
 * communicate with minecraft world
 * */
fun AnchoredCoordinate.Companion.fromFrontLeft(front: Vec3, left: Vec3): AnchoredCoordinate {
    return fromFrontLeft(front.toV3d(), left.toV3d())
}

fun AnchoredCoordinate.anchor(v: Vec3) = anchor(v.x, v.y, v.z)
fun AnchoredCoordinate.anchor(v: Vector3d) = anchor(v.x, v.y, v.z)
fun AnchoredCoordinate.face(v: Vec3) = face(v.x, v.y, v.z)
