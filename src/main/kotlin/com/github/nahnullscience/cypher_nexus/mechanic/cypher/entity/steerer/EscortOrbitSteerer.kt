package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.steerer

import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.DiscardReason
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntityAttributeAccessor.Companion.getExisting
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntityAttributeAccessor.Companion.setAttribute
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import com.github.nahnullscience.cypher_nexus.utility.isServerSide
import com.github.nahnullscience.cypher_nexus.utility.plus
import com.github.nahnullscience.cypher_nexus.utility.times
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import kotlin.math.PI

/**
 * orbit around owner. assume the `owner` is another cypher-entity.
 * */
open class EscortOrbitSteerer(resource: Identifier) : AbstractCypherSteerer(resource) {
    companion object {
        private const val RAD_PER_TICK = (PI / 8).toFloat()

        private val phases: Array<Vec3> // phase vectors are constant, this will save trigonometry calculations at runtime.
        init {
            val tmp = mutableListOf<Vec3>()
            for (i in 0 .. 15) {
                tmp.add(Vec3(1.0, 0.0, 0.0).yRot(RAD_PER_TICK * i))
            }
            phases = tmp.toTypedArray()
        }
    }

    override fun <CE> init(ce: CE) where CE : ICypherEntity, CE : Entity {
        ce.enableFlag(CypherFlags.MOTION_FOLLOWS_OWNER)
        ce.setAttribute(CypherAttributes.BOUNCE, 0.0)
        ce.setAttribute(CypherAttributes.GRAVITY_FACTOR, 0.0)
        ce.setAttribute(CypherAttributes.FRICTION_FACTOR, 0.0)

        ce.owner?.let {
            if (it is ICypherEntity)
                ce.setAttribute(CypherAttributes.EXISTING, it.getExisting().toDouble())
        }
    }

    override fun <CE> tick(ce: CE) where CE : ICypherEntity, CE : Entity {
        if (ce.level().isServerSide && ce.owner?.isRemoved ?: true) {
            ce.discardCypher(DiscardReason.ERASE) // die with owner
        }
    }

    override fun <CE> tickSpeedOverride(ce: CE) where CE : ICypherEntity, CE : Entity {
        ce.owner?.let { owner ->
            val center = owner.position() + owner.knownMovement // position() return ce's BB center
            val scale = owner.bbWidth + 0.75f
            val phase = (ce.tickCount - 1) and 15
            val speed = phases[phase] * scale
            ce.deltaMovement = ce.position().vectorTo(center + speed)
        }
    }


    override fun <CE> discard(ce: CE, reason: DiscardReason) where CE : ICypherEntity, CE : Entity = Unit
}
