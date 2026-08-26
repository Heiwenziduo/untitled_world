package com.github.nahnullscience.cypher_nexus.mechanic.entity.collision

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.ModDataAttachments
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap
import it.unimi.dsi.fastutil.objects.ReferenceArrayList
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import kotlin.math.abs
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

        // debug
//        if (timeI and 127 == 127) {
//            println("${level.sideString()} side [${level.dimension()}] tick entity-storage: ")
//            grids.forEach { (lng, grid) ->
//                println(grid)
//            }
//        }

        //
        gridsSizeMax = max(gridsSizeMax, grids.size)

        // clear the stale
        if (timeI and 31 == 31) {
            gridsTrashCan.removeIf { key ->
                val grid = grids.get(key) ?: return@removeIf true
                if (grid.isNotEmpty) return@removeIf true
                if (time - grid.lastModifyGameTime > 50L) {
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

    /**
     * dedup for a single ray-cast call: which entities have already been AABB-tested, keyed by a
     * monotonic stamp instead of a per-call clear() - clearing a fastutil open map walks its whole
     * backing array, while bumping [raySeq] and comparing is O(1) regardless of how large the map
     * has grown. periodically wiped in [levelTick] so it doesn't hold a reference to every entity
     * ever ray-tested for the level's whole lifetime.
     * */
    private val rayVisitedStamp: Reference2LongOpenHashMap<Entity> =
        Reference2LongOpenHashMap<Entity>(64).apply { defaultReturnValue(-1L) }
    private var raySeq: Long = 0L

    /** reused per grid-cell processed by [rayCastForEach] - see the note inside for why */
    private val candidateScratch: ReferenceArrayList<Entity> = ReferenceArrayList(16)


    inline fun forEachEntityRayCast(
        xp: Double, yp: Double, zp: Double,
        xd: Double, yd: Double, zd: Double,
        margin: Double = Double.NaN,
        crossinline selector: (entity: Entity) -> Boolean = { true },
        crossinline then: (entity: Entity, t: Double) -> Unit
    ) {
        val time = level.gameTime
        var minX = xp; var minY = yp; var minZ = zp
        var maxX = xp; var maxY = yp; var maxZ = zp
        val entitySet = IntOpenHashSet()

        forEachStoredSection(xp, yp, zp, xd, yd, zd, margin, {
            minX1: Double, minY1: Double, minZ1: Double, maxX1: Double, maxY1: Double, maxZ1: Double ->
            minX = minX1; minY = minY1; minZ = minZ1
            maxX = maxX1; maxY = maxY1; maxZ = maxZ1
        }) { index, key, grid ->
            grid.forEachEntity(time, xp, yp, zp, xd, yd, zd, minX, minY, minZ, maxX, maxY, maxZ, margin, selector) { entity, t ->
                // fulfill the selector, actually pierced by the vector, and is new to the loop
                if (entitySet.add(entity.id)) then(entity, t)
            }
        }
    }

    /**
     * casts a segment from (xp,yp,zp) to (xp+xd, yp+yd, zp+zd) - [xd]/[yd]/[zd] is the full
     * displacement, not a unit direction. [margin] inflates every candidate's AABB.
     * only searches [CETargetStorageGrid.targets] - items are
     * deliberately never hit by this, matching why they're tracked separately in the first place.
     *
     * NOTE two small deviations from your draft: [then] now also receives the hit's parametric `t`
     * (already computed, essentially free, and almost certainly wanted - hit point is
     * `xp + xd*t, yp + yd*t, zp + zd*t`), and there's an optional [selector] predicate (mirrors
     * `Level.forEachEntityWithin`'s pattern) so callers can filter - e.g. `ce.canHitTarget` - before
     * the AABB test runs rather than after. Trivial to drop either if you'd rather keep the
     * signature exactly as sketched.
     *
     * @return true if at least one entity intersected the segment
     * */
    fun rayCastForEach(
        xp: Double, yp: Double, zp: Double,
        xd: Double, yd: Double, zd: Double,
        margin: Double,
        selector: (entity: Entity) -> Boolean = { true },
        then: (entity: Entity, t: Double) -> Unit
    ): Boolean {
        val gameTime = level.gameTime
        raySeq++
        var hitAny = false

        rayMarchCells(xp, yp, zp, xd, yd, zd) { cellX, cellY, cellZ, _, _ ->
            val grid = gridAtCellOrNull(cellX, cellY, cellZ)
            if (grid != null) {
                val bit = 1L shl gridBitIndex(cellX, cellY, cellZ)
                if (grid.freshEntityMask(gameTime) and bit != 0L) {
                    candidateScratch.clear()
                    val it = grid.targets.reference2LongEntrySet().iterator()
                    while (it.hasNext()) {
                        val entry = it.next()
                        if (entry.longValue and bit == 0L) continue // present in the grid, not this exact cell
                        val entity = entry.key
                        if (rayVisitedStamp.put(entity, raySeq) == raySeq) continue // already tested this call
                        if (selector(entity)) candidateScratch.add(entity)
                    }
                    // resolve AFTER the entrySet iterator above is done - `then` may synchronously
                    // discard/kill an entity, which can re-enter this grid's `targets` map via
                    // removeEntity() mid-iteration otherwise. same hazard already called out in
                    // CEPhysicsBasics.loopHitAndBounce for the vanilla entity-list equivalent.
                    for (i in candidateScratch.indices) {
                        val entity = candidateScratch[i]
                        val bb = entity.boundingBox
                        val t = rayAABBEntryT(
                            xp, yp, zp, xd, yd, zd,
                            bb.minX - margin, bb.minY - margin, bb.minZ - margin,
                            bb.maxX + margin, bb.maxY + margin, bb.maxZ + margin
                        )
                        if (t >= 0.0) { then(entity, t); hitAny = true }
                    }
                }
            }
            false // never early-exit - caller wants every hit along the whole segment
        }

        return hitAny
    }

    /**
     * same traversal as [rayCastForEach], but returns only the nearest hit and stops walking cells
     * as soon as nothing farther out could possibly beat what's already found - see the write-up
     * above [rayMarchCells] usage for why that's exact, not approximate. no callback invoked
     * mid-traversal, so unlike [rayCastForEach] there's no reentrancy hazard to guard against here.
     * */
    fun rayCastFirst(
        xp: Double, yp: Double, zp: Double,
        xd: Double, yd: Double, zd: Double,
        margin: Double,
        selector: (entity: Entity) -> Boolean = { true },
    ): Entity? {
        val gameTime = level.gameTime
        raySeq++

        var bestEntity: Entity? = null
        var bestT = Double.POSITIVE_INFINITY

        rayMarchCells(xp, yp, zp, xd, yd, zd) { cellX, cellY, cellZ, _, tExit ->
            val grid = gridAtCellOrNull(cellX, cellY, cellZ)
            if (grid != null) {
                val bit = 1L shl gridBitIndex(cellX, cellY, cellZ)
                if (grid.freshEntityMask(gameTime) and bit != 0L) {
                    val it = grid.targets.reference2LongEntrySet().iterator()
                    while (it.hasNext()) {
                        val entry = it.next()
                        if (entry.longValue and bit == 0L) continue
                        val entity = entry.key
                        if (rayVisitedStamp.put(entity, raySeq) == raySeq) continue
                        if (!selector(entity)) continue

                        val bb = entity.boundingBox
                        val t = rayAABBEntryT(
                            xp, yp, zp, xd, yd, zd,
                            bb.minX - margin, bb.minY - margin, bb.minZ - margin,
                            bb.maxX + margin, bb.maxY + margin, bb.maxZ + margin
                        )
                        if (t in 0.0..<bestT) { bestT = t; bestEntity = entity }
                    }
                }
            }
            bestEntity != null && bestT <= tExit // nothing beyond this cell can be closer
        }

        return bestEntity
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
    @PublishedApi
    internal inline fun rayMarchCells(
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

    @PublishedApi
    internal fun axisTMax(p: Double, d: Double, cell: Int, step: Int): Double {
        if (step == 0) return Double.POSITIVE_INFINITY
        val boundary = (cell + if (step > 0) 1 else 0) * CELL_SIZE
        return (boundary - p) / d
    }

    // ================================================================================================
    // per-tick entry points
    // ================================================================================================

    fun updateEntity(entity: Entity) {
        val bb = entity.boundingBox
        val xl = bb.minX.toBlockPos(); val yl = bb.minY.toBlockPos(); val zl = bb.minZ.toBlockPos()
        val xm = bb.maxX.toBlockPos(); val ym = bb.maxY.toBlockPos(); val zm = bb.maxZ.toBlockPos()

        // checked BEFORE touching the tracker - rejecting after needGridPosUpdate() already
        // committed the new coarse bounds would desync the tracker from what's actually stored
        // in `grids` forever (see review notes above)
        if (isSpanInsane(xl, yl, zl, xm, ym, zm)) {
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
     * retracts an entity from every previously-occupied section the new range no longer covers,
     * freeing any grid this empties. short-circuits immediately if the range didn't actually
     * change section-wise (moved within the same section(s) it already occupied) - the common
     * "crossed a 4-block grid boundary but stayed in the same section" case.
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
     * if a overlapping section is empty (no mapping), that key will be skipped.
     *
     * @param vectorConsumer can be used to export calculated min & max point of the vector
     * */
    @PublishedApi
    internal inline fun forEachStoredSection(
        xp: Double, yp: Double, zp: Double,
        xd: Double, yd: Double, zd: Double,
        margin: Double = Double.NaN,
        vectorConsumer: (minX: Double, minY: Double, minZ: Double, maxX: Double, maxY: Double, maxZ: Double) -> Unit =
            { _, _, _, _, _, _, -> },
        action: (index: Int, key: Long, grid: CETargetStorageGrid) -> Unit
    ) {
        vectorMinMaxPoint(xp, yp, zp, xd, yd, zd, margin) { minX, minY, minZ, maxX, maxY, maxZ ->
            vectorConsumer(minX, minY, minZ, maxX, maxY, maxZ)

            val sxMin = minX.pos2SectionCoo(); val syMin = minY.pos2SectionCoo(); val szMin = minZ.pos2SectionCoo()
            val sxMax = maxX.pos2SectionCoo(); val syMax = maxY.pos2SectionCoo(); val szMax = maxZ.pos2SectionCoo()
            var i = 0
            for (sx in sxMin..sxMax) for (sy in syMin..syMax) for (sz in szMin..szMax) {
                if (!sectionContainsVector(sx, sy, sz, xp, yp, zp, xd, yd, zd, margin)) continue
                val key = packSection(sx, sy, sz)
                val grid = grids.get(key) ?: continue
                action(i++, key, grid)
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
    private fun isSpanInsane(xl: Int, yl: Int, zl: Int, xm: Int, ym: Int, zm: Int): Boolean {
        return  (xm - xl) > BLOCK_SPAN_LIMIT ||
                (ym - yl) > BLOCK_SPAN_LIMIT ||
                (zm - zl) > BLOCK_SPAN_LIMIT
    }


    companion object {
        private const val PACK_22: Long = 0b0011_1111_1111_1111_1111_1111L
        private const val PACK_20: Long = 0b1111_1111_1111_1111_1111L

        const val BLOCK_SPAN_LIMIT = 8 * 16
        const val CELL_SIZE = 4.0
        const val CELL_SIZE_INV = 1.0 / CELL_SIZE
        const val MAX_RAY_STEPS = 256


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
            if (xd > 0) maxZ += zd else if (zd < 0) minZ += zd
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


        /**
         * raw-double segment vs AABB slab test.
         *
         * xyz position & direction represent the vector in space.
         *
         * min & max represent the least & most significant points of a virtual AABB.
         * @return entry t in `[0, 1]`, or -1.0 if the segment misses the box
         * @see com.github.nahnullscience.cypher_nexus.utility.checkAABBIntersection
         * */
        @Suppress("DuplicatedCode")
        fun rayAABBEntryT(
            xp: Double, yp: Double, zp: Double,
            xd: Double, yd: Double, zd: Double,
            minX: Double, minY: Double, minZ: Double,
            maxX: Double, maxY: Double, maxZ: Double,
            margin: Double = Double.NaN
        ): Double {
            var tEntry = 0.0
            var tExit = 1.0

            if (xd == 0.0) { if (xp !in minX..maxX) return -1.0 }
            else {
                var t1 = (minX - xp) / xd; var t2 = (maxX - xp) / xd
                if (t1 > t2) { val s = t1; t1 = t2; t2 = s } // let t1 always be the entry time, t2 the exit
                if (t1 > tEntry) tEntry = t1
                if (t2 < tExit) tExit = t2
                if (tEntry > tExit) return -1.0
            }
            if (yd == 0.0) { if (yp !in minY..maxY) return -1.0 }
            else {
                var t1 = (minY - yp) / yd; var t2 = (maxY - yp) / yd
                if (t1 > t2) { val s = t1; t1 = t2; t2 = s }
                if (t1 > tEntry) tEntry = t1
                if (t2 < tExit) tExit = t2
                if (tEntry > tExit) return -1.0
            }
            if (zd == 0.0) { if (zp !in minZ..maxZ) return -1.0 }
            else {
                var t1 = (minZ - zp) / zd; var t2 = (maxZ - zp) / zd
                if (t1 > t2) { val s = t1; t1 = t2; t2 = s }
                if (t1 > tEntry) tEntry = t1
                if (t2 < tExit) tExit = t2
                if (tEntry > tExit) return -1.0
            }
            return tEntry
        }
    }
}
