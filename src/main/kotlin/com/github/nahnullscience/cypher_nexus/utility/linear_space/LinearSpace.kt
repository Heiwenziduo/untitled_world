//package com.github.nahnullscience.cypher_nexus.utility.linear_space
//
//import org.joml.Quaterniond
//import org.joml.Vector3d
//import org.joml.Vector3dc
//
//interface ISpaceAnchor {
//    val anchor: Vector3dc
//}
//
//interface IPlaneDefinition : ISpaceAnchor {
//    val normal: Vector3dc
//
//    companion object {
//        typealias ILineDefinition = IPlaneDefinition
//        inline val ILineDefinition.tangent get() = normal
//    }
//}
//
//interface ICoordinateDefinition : IPlaneDefinition, ISpaceAnchor {
//    val x: Vector3dc
//    val y: Vector3dc
//    val z: Vector3dc
//}
//
//open class Plane(
//    override val normal: Vector3d,
//    override val anchor: Vector3d
//) : IPlaneDefinition {
//    fun rotate(q: Quaterniond): Plane = apply {
//        normal.rotate(q)
//    }
//
////    override fun equals(other: Any?): Boolean {
////        if (this === other) return true
////        if (other !is IPlaneDefinition) return false
////        return normal.equals(other.normal, 1e-6) && anchor.equals(other.anchor, 1e-6)
////    }
////
////    override fun hashCode(): Int {
////        var result = normal.hashCode()
////        result = 31 * result + anchor.hashCode()
////        return result
////    }
//}
//
//open class Coordinate(
//    override val x: Vector3d,
//    override val y: Vector3d,
//    override val z: Vector3d,
//    override val anchor: Vector3d
//) : ICoordinateDefinition {
//    override val normal get() = z
//    fun rotate(q: Quaterniond): Coordinate = apply {
//        x.rotate(q)
//        y.rotate(q)
//        z.rotate(q)
//    }
//}
//
//
