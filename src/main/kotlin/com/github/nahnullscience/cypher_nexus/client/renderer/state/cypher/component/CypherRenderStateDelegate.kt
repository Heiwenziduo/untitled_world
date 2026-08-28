package com.github.nahnullscience.cypher_nexus.client.renderer.state.cypher.component

import com.github.nahnullscience.cypher_nexus.init.config.ModClientConfig
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.BouncePointsManager
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.BouncePointsManager.Companion.polylineInterpolate
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntityAttributeAccessor.Companion.getEffectRadius
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.flag.CypherFlags
import com.github.nahnullscience.cypher_nexus.utility.i.IFlagExtension.Companion.containsFlag
import net.minecraft.client.renderer.entity.state.EntityRenderState
import net.minecraft.util.LightCoordsUtil
import net.minecraft.world.entity.Entity


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
    override var effectRadius: Float = 1f
        private set
    override var bouncePoints: BouncePointsManager? = null
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
            state.lightCoords = LightCoordsUtil.FULL_BRIGHT

        } else if (flags.containsFlag(CypherFlags.IGNORE_BLOCK)) {
            // avoid full black when phase through blocks
            val block = LightCoordsUtil.block(state.lightCoords).coerceAtLeast(3)
            val sky = LightCoordsUtil.sky(state.lightCoords)
            state.lightCoords = LightCoordsUtil.pack(block, sky)
        }

        if (ModClientConfig.CONFIG.bouncePointsInterpolate.isTrue && bouncePoints?.isNotEmpty() ?: false) {
            polylineInterpolate(
                ce.xOld, ce.yOld, ce.zOld,
                ce.x, ce.y, ce.z,
                bouncePoints!!,
                partialTicks
            ) { x, y, z ->
                state.x = x
                state.y = y
                state.z = z
            }
        }
    }
}
