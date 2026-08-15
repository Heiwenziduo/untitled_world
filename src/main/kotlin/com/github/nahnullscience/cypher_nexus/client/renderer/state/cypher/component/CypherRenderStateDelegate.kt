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
    override var vx: Double = 0.0
        private set
    override var vy: Double = 0.0
        private set
    override var vz: Double = 0.0
        private set
    override var xRot: Float = 0f
        private set
    override var yRot: Float = 0f
        private set
    override var flags: Int = 0
        private set
    override var effectRadius: Float = CypherAttributes.EFFECT_RADIUS.value().defaultValue.toFloat()
        private set
    override var bouncePoints: List<Vec3> = listOf()
        private set



    override fun <CE> extractFrom(ce: CE, state: EntityRenderState) where CE : Entity, CE : ICypherEntity {
        val partialTicks = state.partialTick
        val delta = ce.deltaMovement
        vx = delta.x
        vy = delta.y
        vz = delta.z
        xRot = ce.getXRot(partialTicks)
        yRot = ce.getYRot(partialTicks)
        flags = ce.enabledFlags
        effectRadius = ce.getEffectRadius()
        bouncePoints = ce.bouncePoints

        if (flags.containsFlag(CypherFlags.GLOWING) || flags.containsFlag(CypherFlags.PENETRATE_WORLD)) {
            // val block = LightCoordsUtil.block(state.lightCoords)
            state.lightCoords = LightCoordsUtil.pack(15, 15) // full light when glow
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
