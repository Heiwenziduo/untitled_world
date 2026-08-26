package com.github.nahnullscience.cypher_nexus.mechanic.entity.collision

import com.github.nahnullscience.cypher_nexus.mechanic.entity.collision.CETargetStorageGridsManager.Companion.unpackSectionX
import com.github.nahnullscience.cypher_nexus.mechanic.entity.collision.CETargetStorageGridsManager.Companion.unpackSectionY
import com.github.nahnullscience.cypher_nexus.mechanic.entity.collision.CETargetStorageGridsManager.Companion.unpackSectionZ
import com.github.nahnullscience.cypher_nexus.utility.forEachLong
import com.github.nahnullscience.cypher_nexus.utility.rayAABBCollision
import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap
import net.minecraft.core.Direction
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.phys.AABB
import kotlin.math.abs
import kotlin.math.floor

/**
 * storage for one 16x16x16 section: which entities/items are in it, and — per occupant — which
 * of the 64 4x4x4 sub-cells its AABB currently touches, folded per-kind into [entityGridMask] /
 * [itemGridMask] so a future ray-march can test a whole cell against a single `and` before it
 * ever has to look at an individual entity.
 * */
class CETargetStorageGrid private constructor (
    val sectionKey: Long,
    val skX: Int, val skY: Int, val skZ: Int
) {
    constructor(sectionKey: Long): this(sectionKey, sectionKey.unpackSectionX(), sectionKey.unpackSectionY(), sectionKey.unpackSectionZ())

    @PublishedApi
    internal val entities: Reference2LongOpenHashMap<Entity> =
        Reference2LongOpenHashMap<Entity>().apply { defaultReturnValue(0L) }
    @PublishedApi
    internal val items: Reference2LongOpenHashMap<ItemEntity> =
        Reference2LongOpenHashMap<ItemEntity>().apply { defaultReturnValue(0L) }

    @PublishedApi
    internal var entityGridMask: Long = 0L
        private set
    @PublishedApi
    internal var itemGridMask: Long = 0L
        private set

    @PublishedApi
    internal var entityMaskDirty = true
        private set
    @PublishedApi
    internal var itemMaskDirty = true
        private set

    /** true once neither map holds anything — the manager drops the grid from its section map when this flips */
    val isEmpty: Boolean get() = entities.isEmpty() && items.isEmpty()
    val isNotEmpty: Boolean get() = entities.isNotEmpty() || items.isNotEmpty()

    val size: Int get() = entities.size + items.size

    var lastModifyGameTime: Long = -1L
        private set

    var lastSortGameTime: Long = -1L
        private set

    // ------------------------------------------------------------------------------------------
    // extraction
    // ------------------------------------------------------------------------------------------

    /**
     * go through entities that pierced by the given vector,
     * this function will sort the grid-mask and allow consecutive calls cheaper.
     * */
    inline fun forEachEntity(
        time: Long,
        xp: Double, yp: Double, zp: Double,
        xd: Double, yd: Double, zd: Double,
        minX: Double, minY: Double, minZ: Double,
        maxX: Double, maxY: Double, maxZ: Double,
        margin: Double = Double.NaN,
        selector: (entity: Entity) -> Boolean = { true },
        then: (entity: Entity, t: Double, direction: Direction?) -> Unit
    ) {
        // a crude aabb-like grid cover for a vector may prove more efficient than an exact grid-marching (on small number)
        val vecMask = computeGridMask(minX, minY, minZ, maxX, maxY, maxZ, skX, skY, skZ)

        // with the monotonically increasing nature of mask computation,
        // this can prove an exact "miss", even the mask is dirty
        if (vecMask and entityGridMask == 0L) return

        var sortMask = 0L
        entities.forEachLong { entity, lng ->
            sortMask = sortMask or lng
            if (vecMask and lng == 0L) return@forEachLong // no overlapping
            if (!selector(entity)) return@forEachLong // selector not fulfilled

            val bb = entity.boundingBox
            rayAABBCollision(xp, yp, zp, xd, yd, zd, bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, margin)
            { t, direction ->
                then(entity, t, direction)
            }
        }

        sortMask(time, sortMask)
        // a performance freak may also want to detect the total amount of entities then decide the effort for sorting
        // small size just go through every one,
        // large size do a more precise grid computation (DDA) or even arrange an int[0, 63]->Entity[] map on the first call
    }

    inline fun forEachItem(
        time: Long,
        xp: Double, yp: Double, zp: Double,
        xd: Double, yd: Double, zd: Double,
        minX: Double, minY: Double, minZ: Double,
        maxX: Double, maxY: Double, maxZ: Double,
        margin: Double = Double.NaN,
        selector: (entity: Entity) -> Boolean = { true },
        then: (entity: Entity, t: Double) -> Unit
    ) {
        // TODO
    }

    // ------------------------------------------------------------------------------------------
    // targets
    // ------------------------------------------------------------------------------------------

    /**
     * NOTE relies on [computeGridMask] never returning 0 for a section this entity's AABB
     * genuinely overlaps (true by construction: each axis's clamped range is always non-empty,
     * so the AND of all three is always non-empty too) so `0L` can double as fastutil's "absent"
     * default-return-value without an extra `containsKey` probe. worth re-checking if you ever
     * touch `computeGridMask`'s clamping.
     * */
    fun addOrUpdateEntity(entity: Entity, bb: AABB, sx: Int, sy: Int, sz: Int) {
        lastModifyGameTime = entity.level().gameTime
        val mask = computeGridMask(bb, sx, sy, sz)
        entities.put(entity, mask)
        entityMaskDirty = true
        entityGridMask = entityGridMask or mask
    }

    /** @return true once this leaves the grid with nothing left in it at all (targets + items) */
    fun removeEntity(entity: Entity): Boolean {
        lastModifyGameTime = entity.level().gameTime
        if (entities.removeLong(entity) != 0L) entityMaskDirty = true
        return isEmpty
    }

    /***/
    fun sortMask(time: Long, mask: Long) {
        entityMaskDirty = false
        lastSortGameTime = time
        entityGridMask = mask
    }

    // ------------------------------------------------------------------------------------------
    // items — kept separate from targets so e.g. an "attract nearby drops" cypher never has to
    // filter a living-entity query, and a homing cypher never trips over item drops
    // ------------------------------------------------------------------------------------------

    fun addOrUpdateItem(item: ItemEntity, bb: AABB, sx: Int, sy: Int, sz: Int) {
        lastModifyGameTime = item.level().gameTime
        val mask = computeGridMask(bb, sx, sy, sz)
        items.put(item, mask)
        itemMaskDirty = true
        itemGridMask = itemGridMask and mask
    }

    fun removeItem(item: ItemEntity): Boolean {
        lastModifyGameTime = item.level().gameTime
        if (items.removeLong(item) != 0L) itemMaskDirty = true
        return isEmpty
    }


    // ------------------------------------------------------------------------------------------

    fun computeGridMask(bb: AABB, sx: Int, sy: Int, sz: Int): Long =
        computeGridMask(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, sx, sy, sz)
    /**
     * Computes the 64-bit `grid` occupancy mask of an AABB clamped to section (sx, sy, sz).
     * Cell indexing: (lx shl 4) + (lz shl 2) + ly where lx, ly, lz in [0, 3].
     *
     * pos min & max alto-wrap, and the result will contain at least one 1-bit.
     */
    fun computeGridMask(
        minX: Double, minY: Double, minZ: Double,
        maxX: Double, maxY: Double, maxZ: Double,
        sx: Int, sy: Int, sz: Int
    ): Long {
        val secOriginX = sx shl 4; val secOriginY = sy shl 4; val secOriginZ = sz shl 4

        // Local 4m sub-cell bounds clamped to [0..3]
        val minLX = minX.pos2GridCooAutoWrap(secOriginX)
        val maxLX = maxX.pos2GridCooAutoWrap(secOriginX)
        val minLY = minY.pos2GridCooAutoWrap(secOriginY)
        val maxLY = maxY.pos2GridCooAutoWrap(secOriginY)
        val minLZ = minZ.pos2GridCooAutoWrap(secOriginZ)
        val maxLZ = maxZ.pos2GridCooAutoWrap(secOriginZ)

        var xM = 0L; var yM = 0L; var zM = 0L
        for (i in minLX .. maxLX) { xM = xM or X[i] }
        for (i in minLY .. maxLY) { yM = yM or Y[i] }
        for (i in minLZ .. maxLZ) { zM = zM or Z[i] }

        return xM and yM and zM
    }

    // ================================================================================================
    // 3D DDA over 4-block cells (Amanatides & Woo) + raw ray-AABB slab test
    // ================================================================================================

    /**
     * walks every 4-block cell the segment (xp,yp,zp) -> (xp+xd, yp+yd, zp+zd) passes through, in
     * strict distance order. [onCell] receives entry and exit parametric t (both `[0,1]`, `exit`
     * doubling as the next cell's entry-t) and returns true to stop early. private+inline so the
     * lambda is pasted directly into `rayCastFirst`/`rayCastForEach` - no closure object, and
     * captured `var`s (bestEntity, bestT, hitAny) stay ordinary locals instead of getting boxed.
     * */
    @Deprecated("just for memorial, don't call")
    private inline fun rayMarchCells(
        xp: Double, yp: Double, zp: Double,
        xd: Double, yd: Double, zd: Double,
        onCell: (cellX: Int, cellY: Int, cellZ: Int, tEnter: Double, tExit: Double) -> Boolean
    ) {
        if (xd == 0.0 && yd == 0.0 && zd == 0.0) {
            onCell(
                floor(xp * CELL_SIZE_INV).toInt(),
                floor(yp * CELL_SIZE_INV).toInt(),
                floor(zp * CELL_SIZE_INV).toInt(),
                0.0, 1.0
            )
            return
        }

        var cellX = floor(xp * CELL_SIZE_INV).toInt()
        var cellY = floor(yp * CELL_SIZE_INV).toInt()
        var cellZ = floor(zp * CELL_SIZE_INV).toInt()

        val stepX = if (xd > 0.0) 1 else if (xd < 0.0) -1 else 0
        val stepY = if (yd > 0.0) 1 else if (yd < 0.0) -1 else 0
        val stepZ = if (zd > 0.0) 1 else if (zd < 0.0) -1 else 0

        var tMaxX = axisTMax(xp, xd, cellX, stepX)
        var tMaxY = axisTMax(yp, yd, cellY, stepY)
        var tMaxZ = axisTMax(zp, zd, cellZ, stepZ)

        val tDeltaX = if (stepX != 0) CELL_SIZE / abs(xd) else Double.POSITIVE_INFINITY
        val tDeltaY = if (stepY != 0) CELL_SIZE / abs(yd) else Double.POSITIVE_INFINITY
        val tDeltaZ = if (stepZ != 0) CELL_SIZE / abs(zd) else Double.POSITIVE_INFINITY

        var t = 0.0
        var steps = 0
        while (t <= 1.0) {
            val tExit = minOf(tMaxX, tMaxY, tMaxZ, 1.0)
            if (onCell(cellX, cellY, cellZ, t, tExit)) return
            if (++steps > MAX_RAY_STEPS) return // safety valve against a pathological/huge segment

            if (tMaxX <= tMaxY && tMaxX <= tMaxZ)   { cellX += stepX; t = tMaxX; tMaxX += tDeltaX }
            else if (tMaxY <= tMaxZ)                { cellY += stepY; t = tMaxY; tMaxY += tDeltaY }
            else                                    { cellZ += stepZ; t = tMaxZ; tMaxZ += tDeltaZ }
        }
    }

    @Deprecated("just for memorial, don't call")
    private fun axisTMax(p: Double, d: Double, cell: Int, step: Int): Double {
        if (step == 0) return Double.POSITIVE_INFINITY
        val boundary = (cell + if (step > 0) 1 else 0) * CELL_SIZE
        return (boundary - p) / d
    }

    override fun toString(): String {
        return "StorageGrid[x: $skX, y: $skY, z: $skZ, entities: ${entities.size}, items: ${items.size}]"
    }

    companion object {
        private val X = LongArray(4) { i -> 0xFFFFL shl (i * 16) }
        private val Z = LongArray(4) { i -> 0x000F_000F_000F_000FL shl (i * 4) }
        private val Y = LongArray(4) { i -> 0x1111_1111_1111_1111L shl i }

        private const val CELL_SIZE = 4.0
        private const val CELL_SIZE_INV = 1.0 / CELL_SIZE
        private const val MAX_RAY_STEPS = 256

        /**
         * @param sc section coordinate << 4
         * @return grid coordinate wrapped in range `[0, 3]`,
         * for position outside current section, the return always 0 or 3 (depends on direction)
         * */
        fun Double.pos2GridCooAutoWrap(sc: Int) = ((floor(this).toInt() - sc) shr 2).coerceIn(0, 3)

        private fun foldMasks(map: Reference2LongOpenHashMap<*>): Long {
            var m = 0L
            val it = map.values.iterator() // LongIterator - unboxed
            while (it.hasNext()) m = m or it.nextLong()
            return m
        }
    }
}
