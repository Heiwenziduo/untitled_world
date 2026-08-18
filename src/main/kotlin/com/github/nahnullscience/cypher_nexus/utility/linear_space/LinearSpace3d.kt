package com.github.nahnullscience.cypher_nexus.utility.linear_space

import org.joml.Quaterniond
import org.joml.Vector3d
import org.joml.Vector3dc
import kotlin.math.abs
import kotlin.math.sqrt

interface ISpaceAnchor {
    val anchor: Vector3dc
    fun anchor(x: Double, y: Double, z: Double): ISpaceAnchor
    fun move(x: Double, y: Double, z: Double): ISpaceAnchor
}

interface IPlaneDefinition : ISpaceAnchor {
    val normal: Vector3dc

    companion object {
        typealias ILineDefinition = IPlaneDefinition
        inline val ILineDefinition.tangent get() = normal
    }
}

interface ICoordinateDefinition : IPlaneDefinition, ISpaceAnchor {
    val x: Vector3dc
    val y: Vector3dc
    val z: Vector3dc
}

open class Plane(
    override val normal: Vector3d,
    override val anchor: Vector3d
) : IPlaneDefinition {
    fun rotate(q: Quaterniond): Plane = apply {
        normal.rotate(q)
    }

    override fun anchor(x: Double, y: Double, z: Double) = apply { anchor.set(x, y, z) }
    override fun move(x: Double, y: Double, z: Double) = apply { anchor.add(x, y, z) }

//    override fun equals(other: Any?): Boolean {
//        if (this === other) return true
//        if (other !is IPlaneDefinition) return false
//        return normal.equals(other.normal, 1e-6) && anchor.equals(other.anchor, 1e-6)
//    }
//
//    override fun hashCode(): Int {
//        var result = normal.hashCode()
//        result = 31 * result + anchor.hashCode()
//        return result
//    }
}

/**
 * assume [x], [y], [z] are unit vectors
 * */
class AnchoredCoordinate(
    override val x: Vector3d,
    override val y: Vector3d,
    override val z: Vector3d,
    override val anchor: Vector3d = Vector3d()
) : ICoordinateDefinition {
    /** make a copy of the origin coordinate */
    constructor(origin: AnchoredCoordinate): this(
        Vector3d(origin.x),
        Vector3d(origin.y),
        Vector3d(origin.z),
        Vector3d(origin.anchor)
    )

    init {
        // maybe do a cross len > 0 check
    }

    override val normal get() = z

    val front get() = z
    val left get() = x
    val up get() = y

    private var _v0Backing: Vector3d? = null
    private val v0 get() = _v0Backing ?: Vector3d().also { _v0Backing = it }

    private var _q0Backing: Quaterniond? = null
    private val q0 get() = _q0Backing ?: Quaterniond().also { _q0Backing = it }

    private var _vectorCacheBacking: DoubleArray? = null
    @PublishedApi
    internal val vectorCache = _vectorCacheBacking ?: DoubleArray(CACHE_SIZE) { Double.NaN }.also { _vectorCacheBacking = it }

    fun copy() = AnchoredCoordinate(this)

    override fun anchor(x: Double, y: Double, z: Double) = apply { anchor.set(x, y, z) }

    fun rotate(q: Quaterniond) = apply {
        x.rotate(q)
        y.rotate(q)
        z.rotate(q)
    }

    fun rotateX(rad: Double) = apply {
        val ax = x.x; val ay = x.y; val az = x.z
        y.rotateAxis(rad, ax, ay, az)
        z.rotateAxis(rad, ax, ay, az)
    }

    fun rotateY(rad: Double) = apply {
        val ax = y.x; val ay = y.y; val az = y.z
        z.rotateAxis(rad, ax, ay, az)
        x.rotateAxis(rad, ax, ay, az)
    }

    fun rotateZ(rad: Double) = apply {
        val ax = z.x; val ay = z.y; val az = z.z
        x.rotateAxis(rad, ax, ay, az)
        y.rotateAxis(rad, ax, ay, az)
    }

    override fun move(x: Double, y: Double, z: Double) = apply { anchor.add(x, y, z) }

    fun moveForward(factor: Double = 1.0) = apply {
        if (factor == 1.0) anchor.add(front)
        else anchor.add(front.x * factor, front.y * factor, front.z * factor)
    }
    fun moveLeftward(factor: Double = 1.0) = apply {
        if (factor == 1.0) anchor.add(left)
        else anchor.add(left.x * factor, left.y * factor, left.z * factor)
    }
    fun moveUpward(factor: Double = 1.0) = apply {
        if (factor == 1.0) anchor.add(up)
        else anchor.add(up.x * factor, up.y * factor, up.z * factor)
    }

    /**
     * Rigidly reorients the coordinate frame so `front` faces [targetDir].
     * Preserves the relative roll and perpendicularity of `left` and `up`.
     */
    fun face(targetDir: Vector3dc) = face(targetDir.x(), targetDir.y(), targetDir.z())
    /**
     * Rigidly reorients the coordinate frame so `front` faces the given xyz.
     * Preserves the relative roll and perpendicularity of `left` and `up`.
     */
    fun face(x: Double, y: Double, z: Double) = apply {
        val lenSqr = x * x + y * y + z * z
        if (lenSqr < 1e-12) return@apply

        // Normalize target direction using fast scalar inverse sqrt
        val invLen = 1.0 / sqrt(lenSqr)
        val tx = x * invLen
        val ty = y * invLen
        val tz = z * invLen

        // Calculate shortest-arc rotation directly from primitive coordinates
        val q = q0.rotationTo(front.x, front.y, front.z, tx, ty, tz)
        this.rotate(q)
    }

    /**
     * re-orthonormalize the coordinate,
     * or can be used to eliminate accumulated floating-point drift across frequent rotations.
     */
    fun orthonormalize() = apply {
        front.normalize()
        // up.set(front).cross(left).normalize().negate() // Or front.cross(left, up).normalize()
        front.cross(left, up).normalize()
        up.cross(front, left).normalize()
    }

    /**
     *
     * */
    fun putCache() = apply {

    }

    /**
     * number out of bound / vector uninitialized cases will fall back to anchor + forward
     * */
    inline fun extractCache(index: Int, then: vectorsConsumer2) {
        run cache@ {
            if (index < CACHE_PAIR) {
                val start = index * 6
                val xp = vectorCache[start]    .also { if(it.isNaN()) return@cache }
                val yp = vectorCache[start + 1].also { if(it.isNaN()) return@cache }
                val zp = vectorCache[start + 2].also { if(it.isNaN()) return@cache }
                val xd = vectorCache[start + 3].also { if(it.isNaN()) return@cache }
                val yd = vectorCache[start + 4].also { if(it.isNaN()) return@cache }
                val zd = vectorCache[start + 5].also { if(it.isNaN()) return@cache }
                return then(xp, yp, zp, xd, yd, zd)
            }
        }
        return then(anchor.x, anchor.y, anchor.z, front.x, front.y, front.z)
    }

    companion object {
        typealias vectorsConsumer2 = (xp: Double, yp: Double, zp: Double, xd: Double, yd: Double, zd: Double) -> Unit
        @PublishedApi
        internal const val CACHE_PAIR = 8
        @PublishedApi
        internal const val CACHE_SIZE = CACHE_PAIR * 6

        fun fromFrontLeft(front: Vector3d, left: Vector3d): AnchoredCoordinate {
            val up = front.cross(left, Vector3d())
                .normalize() // in case front & left are not strictly perpendicular
            return AnchoredCoordinate(left, up, front)
        }

        fun fromFront(front: Vector3d, fallbackUp: Vector3dc = Vector3d(0.0, 1.0, 0.0)): AnchoredCoordinate {
            val f = Vector3d(front).normalize()
            val l = Vector3d()
            if (abs(f.dot(fallbackUp)) > 0.999) {
                // Pitch singularity: fallback to Z-axis reference
                Vector3d(0.0, 0.0, 1.0).cross(f, l).normalize()
            } else {
                fallbackUp.cross(f, l).normalize()
            }
            val u = Vector3d()
            f.cross(l, u).normalize()
            return AnchoredCoordinate(x = l, y = u, z = f)
        }
    }
}
