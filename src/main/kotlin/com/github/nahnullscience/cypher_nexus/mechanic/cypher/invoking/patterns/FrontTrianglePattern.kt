package com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.patterns

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.AbstractInvokingPattern
import com.github.nahnullscience.cypher_nexus.utility.CoordinateDefinition
import com.github.nahnullscience.cypher_nexus.utility.PosDirePair
import com.github.nahnullscience.cypher_nexus.utility.plus
import com.github.nahnullscience.cypher_nexus.utility.times
import net.minecraft.resources.Identifier
import kotlin.math.sqrt

class FrontTrianglePattern(path: Identifier) : AbstractInvokingPattern(path) {
    companion object {
        private const val LEN = 0.5
        private const val TOP = LEN / 2
        private val SIDE = LEN * sqrt(3.0) / 2
    }
    override fun layout(
        index: Int,
        total: Int,
        coordinate: CoordinateDefinition,
        posDire: PosDirePair
    ): PosDirePair {
        val i = index % 3
        return when(i) {
            1 -> {
                coordinate.getOrComputePatternCache(1) cache@ {
                    val (pos, dir) = posDire
                    val pos1 = pos + coordinate.left * SIDE + coordinate.up * TOP
                    PosDirePair(pos1, dir)
                }
            }
            2 -> {
                coordinate.getOrComputePatternCache(2) cache@ {
                    val (pos, dir) = posDire
                    val pos1 = pos + coordinate.right() * SIDE + coordinate.up * TOP
                    PosDirePair(pos1, dir)
                }
            }
            else -> {
                coordinate.getOrComputePatternCache(0) cache@ {
                    val (pos, dir) = posDire
                    val pos1 = pos + coordinate.down() * LEN
                    PosDirePair(pos1, dir)
                }
            }
        }
    }
}