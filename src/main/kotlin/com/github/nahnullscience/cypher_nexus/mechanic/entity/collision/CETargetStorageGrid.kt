package com.github.nahnullscience.cypher_nexus.mechanic.entity.collision

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
 * of the 64 4x4x4 sub-cells its AABB currently touches, folded per-kind into [girdMask] /
 * [girdMaskItem] so a future ray-march can test a whole cell against a single `and` before it
 * ever has to look at an individual entity.
 * */
class CETargetStorageGrid(
    val sectionKey: Long
) {
    private val targets: Reference2LongOpenHashMap<Entity> =
        Reference2LongOpenHashMap<Entity>().apply { defaultReturnValue(0L) }
    private val items: Reference2LongOpenHashMap<ItemEntity> =
        Reference2LongOpenHashMap<ItemEntity>().apply { defaultReturnValue(0L) }

    private var girdMask: Long = 0L
    private var girdMaskItem: Long = 0L

    /** true once neither map holds anything — the manager drops the grid from its section map when this flips */
    val isEmpty: Boolean get() = targets.isEmpty() && items.isEmpty()
    val isNotEmpty: Boolean get() = targets.isNotEmpty() || items.isNotEmpty()

    val size: Int get() = targets.size + items.size

    var lastModifyGameTime: Long = -1L
        private set

    var lastSortGameTime: Long = -1L
        private set

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
        val old = targets.put(entity, mask)
        // old ⊆ mask → nothing left the union, safe to just OR the new bits in.
        // otherwise the footprint shrank/shifted and some vacated bit might have been exclusive
        // to this entity — fold the grid to find out for sure.
        girdMask = if ((old and mask) == old) girdMask or mask else foldMasks(targets)
    }

    /** @return true once this leaves the grid with nothing left in it at all (targets + items) */
    fun removeEntity(entity: Entity): Boolean {
        lastModifyGameTime = entity.level().gameTime
        val old = targets.removeLong(entity)
        if (old != 0L) girdMask = foldMasks(targets)
        return isEmpty
    }

    // ------------------------------------------------------------------------------------------
    // items — kept separate from targets so e.g. an "attract nearby drops" cypher never has to
    // filter a living-entity query, and a homing cypher never trips over item drops
    // ------------------------------------------------------------------------------------------

    fun addOrUpdateItem(item: ItemEntity, bb: AABB, sx: Int, sy: Int, sz: Int) {
        lastModifyGameTime = item.level().gameTime
        val mask = computeGridMask(bb, sx, sy, sz)
        val old = items.put(item, mask)
        girdMaskItem = if ((old and mask) == old) girdMaskItem or mask else foldMasks(items)
    }

    fun removeItem(item: ItemEntity): Boolean {
        lastModifyGameTime = item.level().gameTime
        val old = items.removeLong(item)
        if (old != 0L) girdMaskItem = foldMasks(items)
        return isEmpty
    }

    // ------------------------------------------------------------------------------------------

    /**
     * Computes the 64-bit `grid` occupancy mask of an AABB clamped to section (sx, sy, sz).
     * Cell indexing: (lx shl 4) + (lz shl 2) + ly where lx, ly, lz in [0, 3].
     */
    fun computeGridMask(bb: AABB, sx: Int, sy: Int, sz: Int): Long {
        val secOriginX = sx shl 4
        val secOriginY = sy shl 4
        val secOriginZ = sz shl 4

        // Local 4m sub-cell bounds clamped to [0..3]
        val minLX = bb.minX.pos2GridCooAutoWrap(secOriginX)
        val maxLX = bb.maxX.pos2GridCooAutoWrap(secOriginX)
        val minLY = bb.minY.pos2GridCooAutoWrap(secOriginY)
        val maxLY = bb.maxY.pos2GridCooAutoWrap(secOriginY)
        val minLZ = bb.minZ.pos2GridCooAutoWrap(secOriginZ)
        val maxLZ = bb.maxZ.pos2GridCooAutoWrap(secOriginZ)

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
        val x = sectionKey.unpackSectionX()
        val y = sectionKey.unpackSectionY()
        val z = sectionKey.unpackSectionZ()
        return "StorageGrid[x: $x, y: $y, z: $z, entities: ${targets.size}, items: ${items.size}]"
    }

    companion object {
        private val X = LongArray(4) { i -> 0xFFFFL shl (i * 16) }
        private val Z = LongArray(4) { i -> 0x000F_000F_000F_000FL shl (i * 4) }
        private val Y = LongArray(4) { i -> 0x1111_1111_1111_1111L shl i }

        /**
         * `[0, 3]`
         * */
        fun Double.pos2GridCoo(): Int = (floor(this).toInt() shr 2) and 0b0011

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
