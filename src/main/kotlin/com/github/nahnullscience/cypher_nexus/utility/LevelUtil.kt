package com.github.nahnullscience.cypher_nexus.utility

import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB

object LevelUtil {

    /**
     * an optimized entity searching function
     * */
    inline fun Level.forEachEntityWithin(
        except: Entity?,
        bb: AABB,
        crossinline selector: (Entity) -> Boolean,
        crossinline action: (Entity) -> Unit
    ) {

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