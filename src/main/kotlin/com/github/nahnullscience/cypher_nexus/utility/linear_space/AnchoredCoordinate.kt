package com.github.nahnullscience.cypher_nexus.utility.linear_space

import org.joml.Quaterniond
import org.joml.Quaternionf
import org.joml.Vector3d
import org.joml.Vector3dc
import org.joml.Vector3f
import kotlin.math.sqrt

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

    override val normal: Vector3dc get() = z

    private var _v0Backing: Vector3d? = null
    val tmpV3d: Vector3d get() = _v0Backing ?: Vector3d().also { _v0Backing = it }

    private var _v1Backing: Vector3f? = null
    val tmpV3f: Vector3f get() = _v1Backing ?: Vector3f().also { _v1Backing = it }

    private var _q0Backing: Quaterniond? = null
    val tmpQd: Quaterniond get() = _q0Backing ?: Quaterniond().also { _q0Backing = it }

    private var _q1Backing: Quaternionf? = null
    val tmpQf: Quaternionf get() = _q1Backing ?: Quaternionf().also { _q1Backing = it }


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
        anchor.add(z.x * factor, z.y * factor, z.z * factor)
    }
    fun moveLeftward(factor: Double = 1.0) = apply {
        anchor.add(x.x * factor, x.y * factor, x.z * factor)
    }
    fun moveUpward(factor: Double = 1.0) = apply {
        anchor.add(y.x * factor, y.y * factor, y.z * factor)
    }


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
        val q = tmpQd.rotationTo(front.x(), front.y(), front.z(), tx, ty, tz)
        this.rotate(q)
    }

    /**
     * re-orthonormalize the coordinate,
     * or can be used to eliminate accumulated floating-point drift across frequent rotations.
     */
    fun orthonormalize() = apply {
        z.normalize()
        // up.set(front).cross(left).normalize().negate() // Or front.cross(left, up).normalize()
        z.cross(x, y).normalize()
        y.cross(z, x).normalize()
    }

    fun hasCache(index: Int): Boolean {
        require(index < CACHE_PAIR)
        val start = index * 6
        return vectorCache[start].isFinite()
    }

    /**
     *
     * */
    fun putCache(index: Int, xp: Double, yp: Double, zp: Double, xd: Double, yd: Double, zd: Double) = apply {
        require(index < CACHE_PAIR)
        val start = index * 6
        vectorCache[start] = xp
        vectorCache[start + 1] = yp
        vectorCache[start + 2] = zp
        vectorCache[start + 3] = xd
        vectorCache[start + 4] = yd
        vectorCache[start + 5] = zd
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
        return then(anchor.x, anchor.y, anchor.z, z.x, z.y, z.z)
    }

    companion object {
        typealias vectorsConsumer2 = (xp: Double, yp: Double, zp: Double, xd: Double, yd: Double, zd: Double) -> Unit
        @PublishedApi
        internal const val CACHE_PAIR = 8
        @PublishedApi
        internal const val CACHE_SIZE = CACHE_PAIR * 6

        /**
         * assume `front` and `left` are unified perpendicular vectors
         * */
        fun fromFrontLeftOrthonormal(front: Vector3d, left: Vector3d): AnchoredCoordinate {
            val up = front.cross(left, Vector3d())
            return AnchoredCoordinate(left, up, front)
        }

        /**
         * assume `front` and `up` are unified perpendicular vectors
         * */
        fun fromFrontUpOrthonormal(front: Vector3d, up: Vector3d): AnchoredCoordinate {
            val left = up.cross(front, Vector3d())
            return AnchoredCoordinate(left, up, front)
        }

        fun genStatic() = AnchoredCoordinate(
            Vector3d(1.0, 0.0, 0.0),
            Vector3d(0.0, 1.0, 0.0),
            Vector3d(0.0, 0.0, 1.0),
        )
    }
}