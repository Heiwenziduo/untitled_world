package com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.patterns

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.AbstractInvokingPattern
import com.github.nahnullscience.cypher_nexus.utility.CoordinateDefinition
import com.github.nahnullscience.cypher_nexus.utility.PosDirePair
import net.minecraft.world.phys.Vec3
import org.joml.Quaternionf
import kotlin.math.PI

object PlaneTrifurcatedPattern : AbstractInvokingPattern("plane_trifurcated") {
    const val RAD = (PI / 6).toFloat()
    override fun layout(
        index: Int,
        total: Int,
        coordinate: CoordinateDefinition,
        posDire: PosDirePair
    ): PosDirePair {
        val i = index % 3
        when (i) {
            1 -> {
                val r = Quaternionf().rotateAxis(RAD, coordinate.top.toVector3f())
                val dire = posDire.direction.toVector3f().rotate(r)
                return PosDirePair(posDire.position, Vec3(dire))
            }
            2 -> {
                val r = Quaternionf().rotateAxis(-RAD, coordinate.top.toVector3f())
                val dire = posDire.direction.toVector3f().rotate(r)
                return PosDirePair(posDire.position, Vec3(dire))
            }
            else -> return posDire
        }
    }
}