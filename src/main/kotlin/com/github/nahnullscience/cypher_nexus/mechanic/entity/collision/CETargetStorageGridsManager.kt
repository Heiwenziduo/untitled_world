package com.github.nahnullscience.cypher_nexus.mechanic.entity.collision

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.ModDataAttachments
import com.github.nahnullscience.cypher_nexus.mechanic.entity.collision.HandleEntityTracking.needTrackingInGrid
import com.github.nahnullscience.cypher_nexus.utility.forEachLong
import com.github.nahnullscience.cypher_nexus.utility.rayAABBEntryT
import com.github.nahnullscience.cypher_nexus.utility.sideString
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.minecraft.core.Direction
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.joml.Vector3dc
import kotlin.math.floor
import kotlin.math.max

/**
 * @see net.minecraft.world.level.entity.PersistentEntitySectionManager
 * @see net.minecraft.world.level.entity.TransientEntitySectionManager
 * */
class CETargetStorageGridsManager(
    val level: Level
) {
    @PublishedApi
    internal val grids: Long2ReferenceOpenHashMap<CETargetStorageGrid> = Long2ReferenceOpenHashMap(32)
    /** anti-shaking objection cache */
    private val gridsTrashCan: LongOpenHashSet = LongOpenHashSet(32)
    private var gridsSizeMax = 0

    val size get() = grids.size

    fun levelTick() {
        val time = level.gameTime
        val timeI = time.toInt()

        // debug // FIXME entity count minor inconsistent on both sides
//        if (timeI and 127 == 127) {
//            println("${level.sideString()} side [${level.dimension()}] tick entity-storage: ")
//            grids.forEachLong { lng, grid ->
//                println(grid)
//            }
//        }

        //
        gridsSizeMax = max(gridsSizeMax, grids.size)

        // clear the stale
        if (timeI and 15 == 15) {
            gridsTrashCan.removeIf { key ->
                val grid = grids.get(key) ?: return@removeIf true
                if (grid.isNotEmpty) return@removeIf true
                if (time - grid.lastModifyGameTime > 5L) {
                    grids.remove(key)
                    return@removeIf true
                }
                return@removeIf false
            }
        }

        if (timeI and 255 == 255) {
            if (gridsSizeMax >= 128 && grids.size <= 32) {
                grids.trim(32)
                gridsTrashCan.trim(32)
            }
        }
    }


    // ================================================================================================
    // ray-cast
    // ================================================================================================

    inline fun forEachEntityRayCast(
        xp: Double, yp: Double, zp: Double,
        xd: Double, yd: Double, zd: Double,
        margin: Double = Double.NaN,
        crossinline selector: (entity: Entity) -> Boolean = { true },
        crossinline then: (entity: Entity, t: Double, direction: Direction?) -> Unit
    ) {
        val time = level.gameTime
        val entitySet = IntOpenHashSet()

        forEachStoredSection(xp, yp, zp, xd, yd, zd, margin)
        { index, key, grid,
          minX, minY, minZ, maxX, maxY, maxZ ->
            grid.forEachEntity(time, xp, yp, zp, xd, yd, zd, minX, minY, minZ, maxX, maxY, maxZ, margin, selector)
            { entity, t, direction ->
                // fulfill the selector, actually pierced by the vector, and is new to the loop
                if (entitySet.add(entity.id)) then(entity, t, direction)
            }
        }
    }


    // ================================================================================================
    // per-tick entry points
    // ================================================================================================

    fun updateEntity(entity: Entity) {
        val bb = entity.boundingBox
        val xl = bb.minX.toBlockPos(); val yl = bb.minY.toBlockPos(); val zl = bb.minZ.toBlockPos()
        val xm = bb.maxX.toBlockPos(); val ym = bb.maxY.toBlockPos(); val zm = bb.maxZ.toBlockPos()

        if (isBlockSpanInsane(xl, yl, zl, xm, ym, zm)) {
            CypherNexus.LOGGER.warn(
                "[{}] AABB spans an implausible block range ({}x{}x{}) - skipping grid tracking this tick",
                entity, xm - xl + 1, ym - yl + 1, zm - zl + 1
            )
            return
        }

        val tracker = entity.getData(ModDataAttachments.CE_TARGET_STATE_TRACKER)
        if (!tracker.needGridPosUpdate(xl, yl, zl, xm, ym, zm)) return

        val sxMin = xl.pos2SectionCoo(); val syMin = yl.pos2SectionCoo(); val szMin = zl.pos2SectionCoo()
        val sxMax = xm.pos2SectionCoo(); val syMax = ym.pos2SectionCoo(); val szMax = zm.pos2SectionCoo()

        val skMin = packSection(sxMin, syMin, szMin)
        val skMax = packSection(sxMax, syMax, szMax)

        if (tracker.isTracked) retractStale(
            skMinOld = tracker.sectionKeyMin, skMaxOld = tracker.sectionKeyMax,
            sxMin = sxMin, syMin = syMin, szMin = szMin,
            sxMax = sxMax, syMax = syMax, szMax = szMax
        ) { key, grid -> grid.removeEntity(entity) }

        if (skMin == skMax) updateSingle(skMin, entity, bb, sxMin, syMin, szMin)
        else updateMultiple(sxMin, syMin, szMin, sxMax, syMax, szMax, entity, bb)

        tracker.updateSectionKey(skMin, skMax)
    }

    fun updateItem(entity: ItemEntity) {
        // TODO item fast access
        // pack position xyz as the sole key for section and ignore the AABB size
    }

    /**
     * call when an entity stops being tracked entirely - death, discard, dimension change, or
     * [HandleEntityTracking.needTrackingInGrid] no longer holding - so it doesn't linger as a dangling reference (and
     * keep the discarded entity itself alive) in whatever grids it last touched. you'll need a
     * hook for this the same way [updateEntity]/[updateItem] need a per-tick driver - nothing in
     * the three files wires either up yet.
     * */
    fun removeEntity(entity: Entity) {
        val tracker = entity.getExistingDataOrNull(ModDataAttachments.CE_TARGET_STATE_TRACKER)
        if (tracker?.isTracked?.not() ?: true) return

        forEachStoredSection(tracker.sectionKeyMin, tracker.sectionKeyMax) { key, grid ->
            val nowEmpty =
                if (entity is ItemEntity) grid.removeItem(entity)
                else grid.removeEntity(entity)
            if (nowEmpty) prepareRemove(key, grid)
            // FIXME(?) delay the removal, for entity may die during extraction loop which result in a mutation
            // println("$entity has been removed from $grid") // this performed after the loop
        }
    }

    // ================================================================================================
    // distribute - single-section is the overwhelmingly common case (most entities are far
    // smaller than 16 blocks), so it skips the loop instead of running a trivial 1x1x1 one
    // ================================================================================================

    private fun updateSingle(sectionKey: Long, entity: Entity, bb: AABB, sx: Int, sy: Int, sz: Int) {
        gridAt(sectionKey) { key, grid, isOld ->
            grid.addOrUpdateEntity(entity, bb, sx, sy, sz)
            // if (isOld) gridsTrashCan.remove(key) // call frequently, maybe periodically go through the trash-can is cheaper
        }
    }

    private fun updateMultiple(
        sxMin: Int, syMin: Int, szMin: Int,
        sxMax: Int, syMax: Int, szMax: Int,
        entity: Entity, bb: AABB
    ) {
        for (sx in sxMin..sxMax) for (sy in syMin..syMax) for (sz in szMin..szMax) {
            val sectionKey = packSection(sx, sy, sz)
            gridAt(sectionKey) { key, grid, isOld ->
                grid.addOrUpdateEntity(entity, bb, sx, sy, sz)
            }
        }
    }

    // ================================================================================================
    // search - locating an entity's *previous* footprint so it can be retracted from whatever
    // grids the new footprint no longer overlaps
    // ================================================================================================

    /**
     * compare the difference between the old min & max (sectionKey) and the new min & max (section-pos).
     *
     * call [removeSection] on those sections that no more covered by the entity, if returns true, the section
     * will be added to trashcan.
     * */
    private inline fun retractStale(
        skMinOld: Long, skMaxOld: Long,
        sxMin: Int, syMin: Int, szMin: Int,
        sxMax: Int, syMax: Int, szMax: Int,
        removeSection: (key: Long, grid: CETargetStorageGrid) -> Boolean
    ) {
        val oxMin = skMinOld.unpackSectionX(); val oyMin = skMinOld.unpackSectionY(); val ozMin = skMinOld.unpackSectionZ()
        val oxMax = skMaxOld.unpackSectionX(); val oyMax = skMaxOld.unpackSectionY(); val ozMax = skMaxOld.unpackSectionZ()

        if (oxMin == sxMin && oyMin == syMin && ozMin == szMin && oxMax == sxMax && oyMax == syMax && ozMax == szMax) return

        // go through old sections, if old not overlap with new, remove
        for (sx in oxMin..oxMax) {
            val inX = sx in sxMin..sxMax
            for (sy in oyMin..oyMax) {
                val inY = inX && sy in syMin..syMax
                for (sz in ozMin..ozMax) {
                    if (inY && sz in szMin..szMax) continue // still covered by the new range
                    val key = packSection(sx, sy, sz)
                    val grid = grids.get(key) ?: continue
                    if (removeSection(key, grid)) prepareRemove(key, grid)
                }
            }
        }
    }

    /**
     * go through sections that intersects with the "cube" structured by the given min & max section keys
     * if a overlapping section is empty (no mapping), that key will be skipped.
     * */
    @PublishedApi
    internal inline fun forEachStoredSection(
        skMin: Long, skMax: Long,
        action: (key: Long, grid: CETargetStorageGrid) -> Unit
    ) {
        val sxMin = skMin.unpackSectionX(); val syMin = skMin.unpackSectionY(); val szMin = skMin.unpackSectionZ()
        val sxMax = skMax.unpackSectionX(); val syMax = skMax.unpackSectionY(); val szMax = skMax.unpackSectionZ()
        return forEachStoredSection(sxMin, syMin, szMin, sxMax, syMax, szMax, action)
    }

    /**
     *
     * */
    @PublishedApi
    internal inline fun forEachStoredSection(
        sxMin: Int, syMin: Int, szMin: Int,
        sxMax: Int, syMax: Int, szMax: Int,
        action: (key: Long, grid: CETargetStorageGrid) -> Unit
    ) {
        for (sx in sxMin..sxMax) for (sy in syMin..syMax) for (sz in szMin..szMax) {
            val key = packSection(sx, sy, sz)
            val grid = grids.get(key) ?: continue
            action(key, grid)
        }
    }

    /**
     * Ray-cast
     *
     * march sections one by one that intersects with the given position & direction vector.
     * if an overlapping section is empty (no mapping), that key will be skipped.
     * */
    @PublishedApi
    internal inline fun forEachStoredSection(
        xp: Double, yp: Double, zp: Double,
        xd: Double, yd: Double, zd: Double,
        margin: Double = Double.NaN,
        action: (index: Int, key: Long, grid: CETargetStorageGrid, minX: Double, minY: Double, minZ: Double, maxX: Double, maxY: Double, maxZ: Double) -> Unit
    ) {
        vectorMinMaxPoint(xp, yp, zp, xd, yd, zd, margin) { minX, minY, minZ, maxX, maxY, maxZ ->
            val xl = minX.toBlockPos(); val yl = minY.toBlockPos(); val zl = minZ.toBlockPos()
            val xm = maxX.toBlockPos(); val ym = maxY.toBlockPos(); val zm = maxZ.toBlockPos()
            if (isBlockSpanInsane(xl, yl, zl, xm, ym, zm)) {
                CypherNexus.LOGGER.warn("ray-cast from ({}, {}, {}) dir ({}, {}, {}) spans an implausible range - aborting", xp, yp, zp, xd, yd, zd)
                return@vectorMinMaxPoint
            }
            val sxMin = minX.pos2SectionCoo(); val syMin = minY.pos2SectionCoo(); val szMin = minZ.pos2SectionCoo()
            val sxMax = maxX.pos2SectionCoo(); val syMax = maxY.pos2SectionCoo(); val szMax = maxZ.pos2SectionCoo()

            var i = 0
            for (sx in sxMin..sxMax) for (sy in syMin..syMax) for (sz in szMin..szMax) {
                if (!sectionContainsVector(sx, sy, sz, xp, yp, zp, xd, yd, zd, margin)) continue
                val key = packSection(sx, sy, sz)
                val grid = grids.get(key) ?: continue
                action(i++, key, grid, minX, minY, minZ, maxX, maxY, maxZ)
            }
        }
    }

    /** primitive-key get-or-create - avoids the boxed-Long path plain `Map.getOrPut` would take here */
    @PublishedApi
    internal fun gridAt(key: Long): CETargetStorageGrid {
        val existing = grids.get(key)
        if (existing != null) return existing
        val fresh = CETargetStorageGrid(key)
        grids.put(key, fresh)
        return fresh
    }

    @PublishedApi
    internal inline fun gridAt(
        key: Long,
        action: (key: Long, grid: CETargetStorageGrid, isOld: Boolean) -> Unit
    ) {
        val existing = grids.get(key)
        if (existing != null) {
            return action(key, existing, true)
        }
        val fresh = CETargetStorageGrid(key)
        grids.put(key, fresh)
        return action(key, fresh, false)
    }

    @PublishedApi
    internal fun gridAtCellOrNull(cellX: Int, cellY: Int, cellZ: Int): CETargetStorageGrid? =
        grids.get(packSection(cellX shr 2, cellY shr 2, cellZ shr 2))

    private fun prepareRemove(key: Long, grid: CETargetStorageGrid) {
        gridsTrashCan.add(key)
    }

    /**
     * guards against a pathological/buggy AABB flooding hundreds of grids in one tick.
     *
     * xyz least & most in the unit of `block-pos`
     * */
    @PublishedApi
    internal fun isBlockSpanInsane(xl: Int, yl: Int, zl: Int, xm: Int, ym: Int, zm: Int): Boolean {
        return  (xm - xl) > BLOCK_SPAN_LIMIT ||
                (ym - yl) > BLOCK_SPAN_LIMIT ||
                (zm - zl) > BLOCK_SPAN_LIMIT
    }

    /***/
    @PublishedApi
    internal fun isSectionSpanInsane(xl: Int, yl: Int, zl: Int, xm: Int, ym: Int, zm: Int): Boolean {
        return  (xm - xl) > SECTION_SPAN_LIMIT ||
                (ym - yl) > SECTION_SPAN_LIMIT ||
                (zm - zl) > SECTION_SPAN_LIMIT
    }


    companion object {
        private const val PACK_22: Long = 0b0011_1111_1111_1111_1111_1111L
        private const val PACK_20: Long = 0b1111_1111_1111_1111_1111L

        const val SECTION_SPAN_LIMIT = 8
        const val BLOCK_SPAN_LIMIT = SECTION_SPAN_LIMIT * 16

        /**
         * @see net.minecraft.core.SectionPos.posToSectionCoord
         * */
        fun Double.pos2SectionCoo(): Int = floor(this).toInt() shr 4

        fun Int.sectionCoo2PosLeast(): Double = (this * 16).toDouble()
        fun Int.sectionCoo2PosMost(): Double = ((this + 1) * 16).toDouble()

        /***/
        fun Double.toBlockPos(): Int = floor(this).toInt()

        /**
         * `[0, 3]`
         * */
        fun Double.pos2GridCoo(): Int = (floor(this).toInt() shr 2) and 0b0011

        /**
         * @see net.minecraft.core.SectionPos.blockToSectionCoord
         * */
        fun Int.pos2SectionCoo(): Int = this shr 4

        /**
         * @see net.minecraft.core.SectionPos.asLong
         * */
        fun packSection(x: Int, y: Int, z: Int): Long {
            var node = 0L
            node = node or ((PACK_22 and x.toLong()) shl 42)
            node = node or ((PACK_20 and y.toLong()) shl 0)
            node = node or ((PACK_22 and z.toLong()) shl 20)
            return node
        }

        /** local occupancy bit for a global grid-cell coordinate: `(lx shl 4) + (lz shl 2) + ly` */
        fun gridBitIndex(cellX: Int, cellY: Int, cellZ: Int): Int {
            val lx = cellX and 0b11
            val ly = cellY and 0b11
            val lz = cellZ and 0b11
            return (lx shl 4) + (lz shl 2) + ly
        }

        // sign-extending inverses of packSection, so a stored section key can be iterated
        // without keeping a redundant int-triple copy of it anywhere
        fun Long.unpackSectionX(): Int = (this shr 42).toInt()
        fun Long.unpackSectionZ(): Int = ((this shl 22) shr 42).toInt()
        fun Long.unpackSectionY(): Int = ((this shl 44) shr 44).toInt()


        /**
         * compute the min(negative) and max(positive) points of the given vector
         * @param margin inflate the virtual AABB by given value, positive -> outward, negative -> inward, only finite value is count
         * @see AABB.inflate
         * */
        inline fun vectorMinMaxPoint(
            xp: Double, yp: Double, zp: Double,
            xd: Double, yd: Double, zd: Double,
            margin: Double = Double.NaN,
            action: (minX: Double, minY: Double, minZ: Double, maxX: Double, maxY: Double, maxZ: Double) -> Unit
        ) {
            var minX = xp; var minY = yp; var minZ = zp
            var maxX = xp; var maxY = yp; var maxZ = zp
            if (xd > 0) maxX += xd else if (xd < 0) minX += xd
            if (yd > 0) maxY += yd else if (yd < 0) minY += yd
            if (zd > 0) maxZ += zd else if (zd < 0) minZ += zd
            if (margin.isFinite()) {
                minX -= margin; minY -= margin; minZ -= margin
                maxX += margin; maxY += margin; maxZ += margin
            }
            action(minX, minY, minZ, maxX, maxY, maxZ)
        }

        /**
         * expand given section-coordinate xyz to min-max position
         * @param margin add additional checking area cover the section, outward if positive, inward if negative. only finite value is count
         * */
        inline fun sectionMinMaxPoint(
            sx: Int, sy: Int, sz: Int,
            margin: Double = Double.NaN,
            action: (minX: Double, minY: Double, minZ: Double, maxX: Double, maxY: Double, maxZ: Double) -> Unit
        ) {
            return if (margin.isFinite()) action(
                    sx.sectionCoo2PosLeast() - margin,
                    sy.sectionCoo2PosLeast() - margin,
                    sz.sectionCoo2PosLeast() - margin,
                    sx.sectionCoo2PosMost() + margin,
                    sy.sectionCoo2PosMost() + margin,
                    sz.sectionCoo2PosMost() + margin
                ) else action(
                sx.sectionCoo2PosLeast(),
                sy.sectionCoo2PosLeast(),
                sz.sectionCoo2PosLeast(),
                sx.sectionCoo2PosMost(),
                sy.sectionCoo2PosMost(),
                sz.sectionCoo2PosMost()
            )
        }

        /**
         * @return true if any part of the vector overlapping with the section
         * */
        fun sectionContainsVector(
            sx: Int, sy: Int, sz: Int,
            xp: Double, yp: Double, zp: Double,
            xd: Double, yd: Double, zd: Double,
            margin: Double = Double.NaN
        ): Boolean {
            sectionMinMaxPoint(sx, sy, sz, margin) { minX, minY, minZ, maxX, maxY, maxZ ->
                val t = rayAABBEntryT(
                    xp, yp, zp,
                    xd, yd, zd,
                    minX, minY, minZ,
                    maxX, maxY, maxZ
                )
                return t >= 0.0
            }
            return false
        }


        inline fun CETargetStorageGridsManager.forEachEntityRayCast(
            position: Vec3, direction: Vec3,
            margin: Double = Double.NaN,
            crossinline selector: (entity: Entity) -> Boolean = { true },
            crossinline then: (entity: Entity, t: Double, direction: Direction?) -> Unit
        ) = this.forEachEntityRayCast(
            position.x, position.y, position.z,
            direction.x, direction.y, direction.z,
            margin, selector, then
        )

        inline fun CETargetStorageGridsManager.forEachEntityRayCast(
            position: Vector3dc, direction: Vector3dc,
            margin: Double = Double.NaN,
            crossinline selector: (entity: Entity) -> Boolean = { true },
            crossinline then: (entity: Entity, t: Double, direction: Direction?) -> Unit
        ) = this.forEachEntityRayCast(
            position.x(), position.y(), position.z(),
            direction.x(), direction.y(), direction.z(),
            margin, selector, then
        )
    }
}
