package com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.patterns

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.AbstractInvokingPattern
import com.github.nahnullscience.cypher_nexus.utility.linear_space.AnchoredCoordinate
import com.github.nahnullscience.cypher_nexus.utility.linear_space.PosDirePair
import net.minecraft.resources.Identifier
import kotlin.math.PI

class PlaneTStylePattern(path: Identifier) : AbstractInvokingPattern(path) {
    companion object {
        private const val RAD = (PI / 2).toFloat()
    }
    override fun arrangeVectors(
        index: Int,
        total: Int,
        coordinate: AnchoredCoordinate
    ): Int {
        val i = index % 3
        return when(i) {
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