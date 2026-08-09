package com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.patterns

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.AbstractInvokingPattern
import com.github.nahnullscience.cypher_nexus.utility.CoordinateDefinition
import com.github.nahnullscience.cypher_nexus.utility.PosDirePair
import net.minecraft.resources.Identifier
import net.minecraft.world.phys.Vec3
import org.joml.Quaternionf
import kotlin.math.PI

class PlaneTrifurcatedPattern(path: Identifier) : AbstractInvokingPattern(path) {
    companion object {
        private const val RAD = (PI / 6).toFloat()
    }
    override fun layout(
        index: Int,
        total: Int,
        coordinate: CoordinateDefinition,
        posDire: PosDirePair
    ): PosDirePair {
        val i = index % 3
        return when (i) {
            1 -> {
                coordinate.getOrComputePatternCache(1) cache@ {
                    val dire = coordinate.rightScrewFromTop(posDire.direction, RAD)
                    return@cache PosDirePair(posDire.position, dire)
                }
            }
            2 -> {
                coordinate.getOrComputePatternCache(2) cache@ {
                    val dire = coordinate.rightScrewFromTop(posDire.direction, -RAD)
                    return@cache PosDirePair(posDire.position, dire)
                }
            }
            else -> posDire
        }
    }
}