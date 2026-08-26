package com.github.nahnullscience.cypher_nexus.mechanic.entity.collision

import com.github.nahnullscience.cypher_nexus.mechanic.entity.collision.CETargetStorageGridsManager.Companion.rayAABBEntryT
import com.github.nahnullscience.cypher_nexus.mechanic.entity.collision.CETargetStorageGridsManager.Companion.unpackSectionX
import com.github.nahnullscience.cypher_nexus.mechanic.entity.collision.CETargetStorageGridsManager.Companion.unpackSectionY
import com.github.nahnullscience.cypher_nexus.mechanic.entity.collision.CETargetStorageGridsManager.Companion.unpackSectionZ
import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.phys.AABB
import kotlin.math.floor

/**
 * storage for one 16x16x16 section: which entities/items are in it, and — per occupant — which
 * of the 64 4x4x4 sub-cells its AABB currently touches, folded per-kind into [gridMask] /
 * [gridMaskItem] so a future ray-march can test a whole cell against a single `and` before it
 * ever has to look at an individual entity.
 * */
class CETargetStorageGrid private constructor (
    val sectionKey: Long,
    val skX: Int, val skY: Int, val skZ: Int
) {
    init {

    }
    constructor(sectionKey: Long): this(sectionKey, sectionKey.unpackSectionX(), sectionKey.unpackSectionY(), sectionKey.unpackSectionZ())

    @PublishedApi
    internal val targets: Reference2LongOpenHashMap<Entity> =
        Reference2LongOpenHashMap<Entity>().apply { defaultReturnValue(0L) }
    @PublishedApi
    internal val items: Reference2LongOpenHashMap<ItemEntity> =
        Reference2LongOpenHashMap<ItemEntity>().apply { defaultReturnValue(0L) }

    @PublishedApi
    internal var gridMask: Long = 0L
        private set
    @PublishedApi
    internal var gridMaskItem: Long = 0L
        private set

    /**
     * true whenever [targets]/[items] changed since the aggregate mask was last folded. checked
     * (and cleared) lazily by [freshEntityMask]/[freshItemMask] instead of inside
     * addOrUpdateEntity/removeEntity themselves - those run every tick for every moving entity,
     * while the aggregate mask is only ever read by a ray-cast, which may not run at all this
     * tick, or may run several times back-to-back. folding once, on demand, right before the
     * first read, means a tick with zero ray-casts pays zero folding cost regardless of how many
     * entities moved.
     * */
    @PublishedApi
    internal var entityMaskDirty = true
        private set
    @PublishedApi
    internal var itemMaskDirty = true
        private set

    /** true once neither map holds anything — the manager drops the grid from its section map when this flips */
    val isEmpty: Boolean get() = targets.isEmpty() && items.isEmpty()
    val isNotEmpty: Boolean get() = targets.isNotEmpty() || items.isNotEmpty()

    val size: Int get() = targets.size + items.size

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
        then: (entity: Entity, t: Double) -> Unit
    ) {
        val needSort = entityMaskDirty || lastModifyGameTime > lastSortGameTime
        var sortMask = 0L

        // a crude aabb-like grid cover for a vector may prove more efficient than an exact grid-marching
        val vecMask = computeGridMask(minX, minY, minZ, maxX, maxY, maxZ, skX, skY, skZ)

        if (!needSort && vecMask and gridMask == 0L) return // no grid overlapping with the vector

        targets.forEach { (entity, lng) ->
            sortMask = sortMask or lng
            if (vecMask and lng == 0L) return@forEach // no overlapping
            if (!selector(entity)) return@forEach // selector not fulfilled

            val bb = entity.boundingBox
            val t = rayAABBEntryT(xp, yp, zp, xd, yd, zd, bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, margin)
            if (t >= 0.0) then(entity, t)
        }

        sortMask(time, sortMask)
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
        targets.put(entity, computeGridMask(bb, sx, sy, sz))
        entityMaskDirty = true
    }

    /** @return true once this leaves the grid with nothing left in it at all (targets + items) */
    fun removeEntity(entity: Entity): Boolean {
        lastModifyGameTime = entity.level().gameTime
        if (targets.removeLong(entity) != 0L) entityMaskDirty = true
        return isEmpty
    }

    /**
     * @return the union of every tracked entity's per-cell occupancy bits, refolding first only
     * if something changed since the last fold - a no-op boolean check on every call after the
     * first, whether that's several cells of the same grid within one ray-cast, or several
     * ray-casts on the same tick.
     * */
    fun freshEntityMask(gameTime: Long): Long {
        if (entityMaskDirty) {
            gridMask = foldMasks(targets)
            entityMaskDirty = false
            lastSortGameTime = gameTime
        }
        return gridMask
    }

    fun sortMask(time: Long, mask: Long) {
        entityMaskDirty = false
        lastSortGameTime = time
        gridMask = mask
    }

    // ------------------------------------------------------------------------------------------
    // items — kept separate from targets so e.g. an "attract nearby drops" cypher never has to
    // filter a living-entity query, and a homing cypher never trips over item drops
    // ------------------------------------------------------------------------------------------

    fun addOrUpdateItem(item: ItemEntity, bb: AABB, sx: Int, sy: Int, sz: Int) {
        lastModifyGameTime = item.level().gameTime
        items.put(item, computeGridMask(bb, sx, sy, sz))
        itemMaskDirty = true
    }

    fun removeItem(item: ItemEntity): Boolean {
        lastModifyGameTime = item.level().gameTime
        if (items.removeLong(item) != 0L) itemMaskDirty = true
        return isEmpty
    }

    fun freshItemMask(gameTime: Long): Long {
        if (itemMaskDirty) {
            gridMaskItem = foldMasks(items)
            itemMaskDirty = false
            lastSortGameTime = gameTime
        }
        return gridMaskItem
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
        val secOriginX = sx shl 4
        val secOriginY = sy shl 4
        val secOriginZ = sz shl 4

        // Local 4m sub-cell bounds clamped to [0..3]
        val minLX = minX.pos2GridCooAutoWrap(secOriginX)
        val maxLX = maxX.pos2GridCooAutoWrap(secOriginX)
        val minLY = minY.pos2GridCooAutoWrap(secOriginY)
        val maxLY = maxY.pos2GridCooAutoWrap(secOriginY)
        val minLZ = minZ.pos2GridCooAutoWrap(secOriginZ)
        val maxLZ = maxZ.pos2GridCooAutoWrap(secOriginZ)

        var xM = 0L
        var yM = 0L
        var zM = 0L
        for (i in minLX .. maxLX) {
            xM = xM or X[i]
        }
        for (i in minLY .. maxLY) {
            yM = yM or Y[i]
        }
        for (i in minLZ .. maxLZ) {
            zM = zM or Z[i]
        }

        return xM and yM and zM
    }

    override fun toString(): String {
        return "StorageGrid[x: $skX, y: $skY, z: $skZ, entities: ${targets.size}, items: ${items.size}]"
    }

    companion object {
        private val X = LongArray(4) { i -> 0xFFFFL shl (i * 16) }
        private val Z = LongArray(4) { i -> 0x000F_000F_000F_000FL shl (i * 4) }
        private val Y = LongArray(4) { i -> 0x1111_1111_1111_1111L shl i }


        /**
         * @param sc section coordinate << 4
         * @return grid coordinate wrapped in range `[0, 3]`,
         * for position outside current section, the return always 0 or 3 (depends on direction)
         * */
        fun Double.pos2GridCooAutoWrap(sc: Int) = ((floor(this).toInt() - sc) shr 2).coerceIn(0, 3)

        private fun foldMasks(map: Reference2LongOpenHashMap<*>): Long {
            var m = 0L
            map.forEach { (any, lng) ->
                m = m or lng
            }
            return m
        }
    }
}
