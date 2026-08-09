package com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.steerer

import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.DiscardReason
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntityAttributeAccessor.Companion.computeAttrWithBase
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntityAttributeAccessor.Companion.setAttribute
import com.github.nahnullscience.cypher_nexus.utility.times
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.Entity
import kotlin.math.pow

/**
 * makes a projectile practively static at the start few ticks, then it recovers back to normal speed.
 * */
open class SlowBootSteerer(resource: Identifier) : AbstractCypherSteerer(resource) {
    companion object {
        private const val INIT_SPEED_MULTIPLIER = 0.05
        private const val START = 20
        private const val ACCELERATE_TICKS = 5
        private const val END = START + ACCELERATE_TICKS
        private val ACCELERATION = (1.0 / INIT_SPEED_MULTIPLIER).pow(1.0 / ACCELERATE_TICKS)
    }

    override fun <CE> init(ce: CE) where CE : ICypherEntity, CE : Entity {
        ce.deltaMovement *= INIT_SPEED_MULTIPLIER
        ce.setAttribute(CypherAttributes.FRICTION_FACTOR, 0.0)
        ce.setAttribute(CypherAttributes.GRAVITY_FACTOR, 0.0)
    }

    override fun <CE> tick(ce: CE) where CE : ICypherEntity, CE : Entity {
        if (ce.tickCount == START) ce.needsSync = true

        if (ce.tickCount in START until END) {
            ce.deltaMovement *= ACCELERATION

        } else if (ce.tickCount == END) {
            ce.needsSync = true
            ce.computeAttrWithBase(CypherAttributes.FRICTION_FACTOR) { it }
            ce.computeAttrWithBase(CypherAttributes.GRAVITY_FACTOR) { it }
        }
    }

    override fun <CE> tickSpeedOverride(ce: CE) where CE : ICypherEntity, CE : Entity = Unit

    override fun <CE> discard(
        ce: CE,
        reason: DiscardReason
    ) where CE : ICypherEntity, CE : Entity = Unit
}