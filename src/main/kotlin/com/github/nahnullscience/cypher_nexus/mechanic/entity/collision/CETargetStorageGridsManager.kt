package com.github.nahnullscience.cypher_nexus.mechanic.entity.collision

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.ModDataAttachments
import com.github.nahnullscience.cypher_nexus.utility.sideString
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import kotlin.math.floor
import kotlin.math.max

/**
 * @see net.minecraft.world.level.entity.PersistentEntitySectionManager
 * @see net.minecraft.world.level.entity.TransientEntitySectionManager
 * */
class CETargetStorageGridsManager(
    val level: Level
) {
    private val grids: Long2ReferenceOpenHashMap<CETargetStorageGrid> = Long2ReferenceOpenHashMap(32)
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
    // per-tick entry points
    // ================================================================================================

    fun updateEntity(entity: Entity) {
        val bb = entity.boundingBox
        val xl = bb.minX.toBlockPos(); val yl = bb.minY.toBlockPos(); val zl = bb.minZ.toBlockPos()
        val xm = bb.maxX.toBlockPos(); val ym = bb.maxY.toBlockPos(); val zm = bb.maxZ.toBlockPos()
        val tracker = entity.getData(ModDataAttachments.CE_TARGET_STATE_TRACKER)

        if (!tracker.needGridPosUpdate(xl, yl, zl, xm, ym, zm)) return

        val sxMin = xl.pos2SectionCoo(); val syMin = yl.pos2SectionCoo(); val szMin = zl.pos2SectionCoo()
        val sxMax = xm.pos2SectionCoo(); val syMax = ym.pos2SectionCoo(); val szMax = zm.pos2SectionCoo()

        if (isSpanInsane(sxMin, syMin, szMin, sxMax, syMax, szMax)) {
            CypherNexus.LOGGER.warn(
                "[{}] AABB spans an implausible section range ({}x{}x{}) - skipping grid tracking this tick",
                entity, sxMax - sxMin + 1, syMax - syMin + 1, szMax - szMin + 1
            )
            return
        }

        val skMin = packSection(sxMin, syMin, szMin)
        val skMax = packSection(sxMax, syMax, szMax)

        if (tracker.isTracked) retractStale(
            skMinOld = tracker.sectionKeyMin, skMaxOld = tracker.sectionKeyMax,
            sxMin = sxMin, syMin = syMin, szMin = szMin,
            sxMax = sxMax, syMax = syMax, szMax = szMax
        ) { it.removeEntity(entity) }

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
        removeWhenStale: (grid: CETargetStorageGrid) -> Boolean
    ) {
        val oxMin = skMinOld.unpackSectionX(); val oyMin = skMinOld.unpackSectionY(); val ozMin = skMinOld.unpackSectionZ()
        val oxMax = skMaxOld.unpackSectionX(); val oyMax = skMaxOld.unpackSectionY(); val ozMax = skMaxOld.unpackSectionZ()

        if (oxMin == sxMin && oyMin == syMin && ozMin == szMin && oxMax == sxMax && oyMax == syMax && ozMax == szMax) return

        for (sx in oxMin..oxMax) {
            val inX = sx in sxMin..sxMax
            for (sy in oyMin..oyMax) {
                val inY = inX && sy in syMin..syMax
                for (sz in ozMin..ozMax) {
                    if (inY && sz in szMin..szMax) continue // still covered by the new range
                    val key = packSection(sx, sy, sz)
                    val grid = grids.get(key) ?: continue
                    if (removeWhenStale(grid)) prepareRemove(key, grid)
                }
            }
        }
    }

    private inline fun forEachStoredSection(
        skMin: Long, skMax: Long,
        action: (key: Long, grid: CETargetStorageGrid) -> Unit
    ) {
        val xMin = skMin.unpackSectionX(); val yMin = skMin.unpackSectionY(); val zMin = skMin.unpackSectionZ()
        val xMax = skMax.unpackSectionX(); val yMax = skMax.unpackSectionY(); val zMax = skMax.unpackSectionZ()
        for (sx in xMin..xMax) for (sy in yMin..yMax) for (sz in zMin..zMax) {
            val key = packSection(sx, sy, sz)
            val grid = grids.get(key) ?: continue
            action(key, grid)
        }
    }

    /** primitive-key get-or-create - avoids the boxed-Long path plain `Map.getOrPut` would take here */
    private fun gridAt(key: Long): CETargetStorageGrid {
        val existing = grids.get(key)
        if (existing != null) return existing
        val fresh = CETargetStorageGrid(key)
        grids.put(key, fresh)
        return fresh
    }

    private inline fun gridAt(
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

    private fun prepareRemove(key: Long, grid: CETargetStorageGrid) {
        gridsTrashCan.add(key)
    }

    /** guards against a pathological/buggy AABB flooding hundreds of grids in one tick */
    private fun isSpanInsane(sxMin: Int, syMin: Int, szMin: Int, sxMax: Int, syMax: Int, szMax: Int): Boolean {
        return  (sxMax - sxMin) > SECTION_SPAN_MAX ||
                (syMax - syMin) > SECTION_SPAN_MAX ||
                (szMax - szMin) > SECTION_SPAN_MAX
    }


    companion object {
        private const val PACK_22: Long = 0b0011_1111_1111_1111_1111_1111L
        private const val PACK_20: Long = 0b1111_1111_1111_1111_1111L

        const val SECTION_SPAN_MAX = 8


        /**
         * @see net.minecraft.core.SectionPos.posToSectionCoord
         * */
        fun Double.pos2SectionCoo(): Int = floor(this).toInt() shr 4

        /***/
        fun Double.toBlockPos(): Int = floor(this).toInt()

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

        // sign-extending inverses of packSection, so a stored section key can be iterated
        // without keeping a redundant int-triple copy of it anywhere
        fun Long.unpackSectionX(): Int = (this shr 42).toInt()
        fun Long.unpackSectionZ(): Int = ((this shl 22) shr 42).toInt()
        fun Long.unpackSectionY(): Int = ((this shl 44) shr 44).toInt()
    }
}
