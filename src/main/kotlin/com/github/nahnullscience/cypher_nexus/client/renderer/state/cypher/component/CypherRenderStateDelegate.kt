package com.github.nahnullscience.cypher_nexus.client.renderer.state.cypher.component

import com.github.nahnullscience.cypher_nexus.init.config.ModClientConfig
import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntityAttributeAccessor.Companion.getEffectRadius
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags.Companion.containsFlag
import net.minecraft.client.renderer.entity.state.EntityRenderState
import net.minecraft.util.LightCoordsUtil
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3


class CypherRenderStateDelegate : ICypherEntityRenderState {
    override var flags: Int = 0
        private set
    override var effectRadius: Float = CypherAttributes.EFFECT_RADIUS.value().defaultValue.toFloat()
        private set
    override var bouncePoints: List<Vec3> = listOf()
        private set
    override var deltaMove: Vec3 = Vec3.ZERO
        private set

    override fun <CE> extractFrom(ce: CE, state: EntityRenderState) where CE : Entity, CE : ICypherEntity {
        flags = ce.enabledFlags
        effectRadius = ce.getEffectRadius()
        bouncePoints = ce.bouncePoints
        deltaMove = ce.deltaMovement

        if (flags.containsFlag(CypherFlags.GLOWING) || flags.containsFlag(CypherFlags.PENETRATE_WORLD)) {
            // full light when glow
            val block = LightCoordsUtil.block(state.lightCoords)
            state.lightCoords = LightCoordsUtil.pack(block, 15)
        } else if (flags.containsFlag(CypherFlags.IGNORE_BLOCK)) {
            // avoid full black when phase block
            val block = LightCoordsUtil.block(state.lightCoords).coerceAtLeast(3)
            val sky = LightCoordsUtil.sky(state.lightCoords)
            state.lightCoords = LightCoordsUtil.pack(block, sky)
        }

//        state.boundingBoxHeight *= ce.getEffectRadius()
//        state.boundingBoxWidth *= ce.getEffectRadius()
        if (ModClientConfig.CONFIG.bouncePointsInterpolate.isTrue && bouncePoints.isNotEmpty()) {

        }
    }
}
