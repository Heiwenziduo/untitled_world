package com.github.nahnullscience.cypher_nexus.client.cypher.state.component

import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags.Companion.containsFlag
import net.minecraft.client.renderer.entity.state.EntityRenderState
import net.minecraft.util.LightCoordsUtil
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3


class CypherRenderState : ICypherEntityRenderState {
    override var flags: Int = 0
        private set
    override var effectRadius: Float = CypherAttributes.EFFECT_RADIUS.value().defaultValue.toFloat()
        private set
    override var bouncePoints: List<Vec3> = listOf()
        private set

    override fun <CE> extractFrom(cy: CE, state: EntityRenderState) where CE : Entity, CE : ICypherEntity {
        flags = cy.enabledFlags
        effectRadius = cy.getEffectRadius()
        bouncePoints = cy.bouncePoints

        if (flags.containsFlag(CypherFlags.GLOWING)) {
            val sky = LightCoordsUtil.sky(state.lightCoords)
            state.lightCoords = LightCoordsUtil.pack(15, sky)
        }
    }
}
