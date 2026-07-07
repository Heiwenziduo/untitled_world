package com.github.nahnullscience.cypher_nexus.utility

import net.minecraft.util.profiling.Profiler
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB

object LevelUtil {
    const val CLIENT = "client"
    const val SERVER = "server"

    fun Level.side(): String = if (isClientSide) CLIENT else SERVER

    /**
     * an optimized(?) entity searching function.
     * */
    inline fun Level.forEachEntityWithin(
        except: Entity?,
        bb: AABB,
        crossinline selector: (Entity) -> Boolean,
        crossinline action: (Entity) -> Unit
    ) {
        Profiler.get().incrementCounter { "getEntities" }
        this.entities.get(bb) { entity ->
            if (entity != except && selector(entity)) {
                action(entity)
            }
        }

        for (part in this.dragonParts()) {
            if (part != except &&
                part.parent != except &&
                selector(part) &&
                bb.intersects(part.boundingBox)
            ) {
                action(part)
            }
        }
    }

}