package com.github.nahnullscience.cypher_nexus.mechanic.entity.collision

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.longs.LongArrayList
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import kotlin.math.abs
import kotlin.math.floor

/**
 *
 * */
class CETargetStorageGrid11111111111111111(val cellSize: Double = 4.0) {
    companion object {
        const val CELL_SIZE: Int = 4
        private fun pack(x: Int, y: Int, z: Int): Long {

        }
    }


    // Entity -> list/array of currently occupied packed section keys
    private val entitySectionOccupancy = Reference2ObjectOpenHashMap<LivingEntity, LongArrayList>()

    private fun updateMultiSections(
        entity: LivingEntity,
        bb: AABB,
        xMin: Int, yMin: Int, zMin: Int,
        xMax: Int, yMax: Int, zMax: Int
    ) {
        val newSections = LongArrayList(8) // Max 8 sections for standard entity bounds

        for (sx in xMin..xMax) {
            for (sz in zMin..zMax) {
                for (sy in yMin..yMax) {
                    val sectionKey = packSection(sx, sy, sz)
                    val mask = computeGridMask(bb, sx, sy, sz)

                    if (mask != 0L) {
                        newSections.add(sectionKey)
                        val tracker = getOrCreateSectionTracker(sectionKey)
                        tracker.updateEntity(entity, mask)
                    }
                }
            }
        }

        // Remove entity from sections it no longer occupies
        val oldSections = entitySectionOccupancy.get(entity)
        if (oldSections != null) {
            for (i in 0 until oldSections.size) {
                val oldKey = oldSections.getLong(i)
                if (!newSections.contains(oldKey)) {
                    getSectionTracker(oldKey)?.removeEntity(entity)
                }
            }
        }

        entitySectionOccupancy.put(entity, newSections)
    }

    private fun updateSingleSection(entity: LivingEntity, sectionKey: Long, mask: Long) {
        val oldSections = entitySectionOccupancy.get(entity)

        // Clear other occupied sections if it previously spanned multiple
        if (oldSections != null) {
            for (i in 0 until oldSections.size) {
                val oldKey = oldSections.getLong(i)
                if (oldKey != sectionKey) {
                    getSectionTracker(oldKey)?.removeEntity(entity)
                }
            }
            oldSections.clear()
            oldSections.add(sectionKey)
        } else {
            val list = LongArrayList(1)
            list.add(sectionKey)
            entitySectionOccupancy.put(entity, list)
        }

        val tracker = getOrCreateSectionTracker(sectionKey)
        tracker.updateEntity(entity, mask)
    }

    // Packed Cell Key -> Entities in this cell
    private val cellBuckets = Long2ObjectOpenHashMap<ReferenceOpenHashSet<Entity>>()
    // Entity -> Array of packed cell keys it currently resides in
    private val entityOccupancy = Reference2ObjectOpenHashMap<Entity, LongArrayList>()

    fun updateEntity(entity: Entity) {
        // if pos0 == pos return

        val bb = entity.boundingBox
        val minX = floor(bb.minX / cellSize).toInt()
        val maxX = floor(bb.maxX / cellSize).toInt()
        val minY = floor(bb.minY / cellSize).toInt()
        val maxY = floor(bb.maxY / cellSize).toInt()
        val minZ = floor(bb.minZ / cellSize).toInt()
        val maxZ = floor(bb.maxZ / cellSize).toInt()

        // 1. Gather all cells overlapping the bounding box
        val newCells = LongArrayList()
        for (x in minX..maxX) {
            for (y in minY..maxY) {
                for (z in minZ..maxZ) {
                    newCells.add(pack(x, y, z))
                }
            }
        }

        val oldCells = entityOccupancy[entity]

        // If cell occupancy hasn't changed, do nothing (0 heap churn)
        if (oldCells != null && oldCells == newCells) return

        // 2. Remove from cells no longer occupied
        oldCells?.forEach { oldKey ->
            if (!newCells.contains(oldKey)) {
                val bucket = cellBuckets.get(oldKey)
                bucket?.remove(entity)
                if (bucket != null && bucket.isEmpty()) {
                    cellBuckets.remove(oldKey)
                }
            }
        }

        // 3. Add to new cells
        for (i in newCells.indices) {
            val key = newCells.getLong(i)
            if (oldCells == null || !oldCells.contains(key)) {
                val bucket = cellBuckets.computeIfAbsent(key) { ReferenceOpenHashSet() }
                bucket.add(entity)
            }
        }

        // 4. Update the cached occupancy
        entityOccupancy[entity] = newCells
    }

    fun removeEntity(entity: LivingEntity) {
        val oldCells = entityOccupancy.remove(entity) ?: return
        oldCells.forEach { key ->
            val bucket = cellBuckets.get(key)
            bucket?.remove(entity)
            if (bucket != null && bucket.isEmpty()) {
                cellBuckets.remove(key)
            }
        }
    }

    fun sweepRay(
        start: Vec3,
        velocity: Vec3,
        isPiercing: Boolean,
        onHit: (Entity, Vec3) -> Unit
    ): HitResult? {
        val totalDistSqr = velocity.lengthSqr()
        if (totalDistSqr < 1e-12) return null

        // 1. Starting Cell Coordinates
        var cx = floor(start.x / cellSize).toInt()
        var cy = floor(start.y / cellSize).toInt()
        var cz = floor(start.z / cellSize).toInt()

        // 2. Step directions (+1 or -1)
        val stepX = if (velocity.x >= 0) 1 else -1
        val stepY = if (velocity.y >= 0) 1 else -1
        val stepZ = if (velocity.z >= 0) 1 else -1

        // 3. Parametric step sizes (tDelta)
        val tDeltaX = if (velocity.x != 0.0) abs(cellSize / velocity.x) else Double.POSITIVE_INFINITY
        val tDeltaY = if (velocity.y != 0.0) abs(cellSize / velocity.y) else Double.POSITIVE_INFINITY
        val tDeltaZ = if (velocity.z != 0.0) abs(cellSize / velocity.z) else Double.POSITIVE_INFINITY

        // 4. Initial boundary distance (tMax)
        var tMaxX = if (velocity.x != 0.0) {
            val nextBoundary = (cx + if (stepX > 0) 1 else 0) * cellSize
            (nextBoundary - start.x) / velocity.x
        } else Double.POSITIVE_INFINITY

        var tMaxY = if (velocity.y != 0.0) {
            val nextBoundary = (cy + if (stepY > 0) 1 else 0) * cellSize
            (nextBoundary - start.y) / velocity.y
        } else Double.POSITIVE_INFINITY

        var tMaxZ = if (velocity.z != 0.0) {
            val nextBoundary = (cz + if (stepZ > 0) 1 else 0) * cellSize
            (nextBoundary - start.z) / velocity.z
        } else Double.POSITIVE_INFINITY

        var closestHit: HitResult? = null
        var closestDistSqr = Double.MAX_VALUE

        // 5. March through intersected cells (t in [0, 1])
        while (true) {
            // Query only the entities registered in the current cell
            val cellKey = pack(cx, cy, cz)
            val targets = grid.get(cellKey)

            if (targets != null) {
                for (target in targets) {
                    val hitPos = target.boundingBox.clip(start, start.add(velocity))
                    if (hitPos != null) {
                        val dSqr = start.distanceToSqr(hitPos)
                        if (dSqr < closestDistSqr) {
                            closestDistSqr = dSqr
                            closestHit = HitResult(target, hitPos)

                            if (isPiercing) {
                                onHit(target, hitPos)
                            }
                        }
                    }
                }
                // Non-piercing: stop as soon as a confirmed hit occurs in the closest processed cell
                if (!isPiercing && closestHit != null) {
                    return closestHit
                }
            }

            // 6. Advance to the next nearest cell boundary
            if (tMaxX < tMaxY && tMaxX < tMaxZ) {
                if (tMaxX > 1.0) break // Traversed past the end of the velocity vector
                cx += stepX
                tMaxX += tDeltaX
            } else if (tMaxY < tMaxZ) {
                if (tMaxY > 1.0) break
                cy += stepY
                tMaxY += tDeltaY
            } else {
                if (tMaxZ > 1.0) break
                cz += stepZ
                tMaxZ += tDeltaZ
            }
        }

        return closestHit
    }

    fun sweepRay(
        start: Vec3,
        velocity: Vec3,
        filter: (LivingEntity) -> Boolean = { true }
    ): HitResult? {
        val totalDistSqr = velocity.lengthSqr()
        if (totalDistSqr < 1e-12) return null

        val end = start.add(velocity)
        var cx = floor(start.x / cellSize).toInt()
        var cy = floor(start.y / cellSize).toInt()
        var cz = floor(start.z / cellSize).toInt()

        val stepX = if (velocity.x >= 0) 1 else -1
        val stepY = if (velocity.y >= 0) 1 else -1
        val stepZ = if (velocity.z >= 0) 1 else -1

        val tDeltaX = if (velocity.x != 0.0) abs(cellSize / velocity.x) else Double.POSITIVE_INFINITY
        val tDeltaY = if (velocity.y != 0.0) abs(cellSize / velocity.y) else Double.POSITIVE_INFINITY
        val tDeltaZ = if (velocity.z != 0.0) abs(cellSize / velocity.z) else Double.POSITIVE_INFINITY

        var tMaxX = if (velocity.x != 0.0) {
            val nextX = (cx + if (stepX > 0) 1 else 0) * cellSize
            (nextX - start.x) / velocity.x
        } else Double.POSITIVE_INFINITY

        var tMaxY = if (velocity.y != 0.0) {
            val nextY = (cy + if (stepY > 0) 1 else 0) * cellSize
            (nextY - start.y) / velocity.y
        } else Double.POSITIVE_INFINITY

        var tMaxZ = if (velocity.z != 0.0) {
            val nextZ = (cz + if (stepZ > 0) 1 else 0) * cellSize
            (nextZ - start.z) / velocity.z
        } else Double.POSITIVE_INFINITY

        // Prevents redundant AABB math on entities spanning multiple traversed cells
        val testedEntities = ReferenceOpenHashSet<LivingEntity>()
        var closestHit: HitResult? = null
        var closestDistSqr = Double.MAX_VALUE

        while (true) {
            val bucket = cellBuckets.get(pack(cx, cy, cz))
            if (bucket != null) {
                for (target in bucket) {
                    if (testedEntities.add(target) && filter(target)) {
                        val hitPos = target.boundingBox.clip(start, end).orElse(null)
                        if (hitPos != null) {
                            val dSqr = start.distanceToSqr(hitPos)
                            if (dSqr < closestDistSqr) {
                                closestDistSqr = dSqr
                                closestHit = HitResult(target, hitPos)
                            }
                        }
                    }
                }
                // Non-piercing: stop as soon as we register a collision in this cell
                if (closestHit != null) return closestHit
            }

            // March to next cell
            if (tMaxX < tMaxY && tMaxX < tMaxZ) {
                if (tMaxX > 1.0) break
                cx += stepX
                tMaxX += tDeltaX
            } else if (tMaxY < tMaxZ) {
                if (tMaxY > 1.0) break
                cy += stepY
                tMaxY += tDeltaY
            } else {
                if (tMaxZ > 1.0) break
                cz += stepZ
                tMaxZ += tDeltaZ
            }
        }

        return closestHit
    }
}
