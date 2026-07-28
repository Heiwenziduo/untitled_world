package com.github.nahnullscience.cypher_nexus.content.cypher.modifier

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap.Builder
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ModifierCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.delegation.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.EntityCaptureHook
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.TickBehaviorHook
import com.github.nahnullscience.cypher_nexus.utility.rotateTowards
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import kotlin.math.PI
import kotlin.math.min

/** these homing-s share the same "homing target" on HooksSharedData */
abstract class AbstractTargetHoming(
    path: String,
    defaultAttribute: Builder.() -> Builder
) : ModifierCypher(defaultAttribute), TickBehaviorHook, EntityCaptureHook {

    companion object {
        private const val HOMING_STRENGTH = 0.1
        private const val HOMING_STRENGTH_LEVEL = 0.02
        private const val ROTATION_RADIUS = PI / 36
    }

    override val resource = CypherNexus.modResource(path)

    override fun <CE> forEntityCaptured(
        index: Int,
        count: Int,
        level: Level,
        cyEntity: CE,
        target: Entity
    ) where CE : Entity, CE : ICypherEntity {
        if (cyEntity.hooksSharedData.homingTarget == target) return
        if (cyEntity.canHomeTarget(target)) {
            if (cyEntity.hooksSharedData.homingTarget == null)
                cyEntity.hooksSharedData.homingTarget = target
            else {
                val old = cyEntity.position().distanceToSqr(cyEntity.hooksSharedData.homingTarget!!.eyePosition)
                val new = cyEntity.position().distanceToSqr(target.eyePosition)
                if (new < old) cyEntity.hooksSharedData.homingTarget = target
            }
        }
    }

    override fun <CE> needCapture(level: Level, cyEntity: CE): Boolean where CE : Entity, CE : ICypherEntity {
        val target = cyEntity.hooksSharedData.homingTarget ?: return true
        return !cyEntity.canHomeTarget(target)
    }

//    protected open fun authenticateTarget(projectile: AbstractCypherProjectile): Boolean {
//        // consider merge with AbstractCypherProjectile#canHomeTarget
//        val target = projectile.hooksSharedData.homingTarget ?: return false
//        if (projectile.distanceToSqr(target.eyePosition) > CAPTURE_SIZE_SQR * 2) {
//            projectile.hooksSharedData.homingTarget = null
//            return false
//        }
//        return true
//    }


    class Homing(defaultAttribute: Builder.() -> Builder) : AbstractTargetHoming("homing", defaultAttribute) {
        override fun <CE> onTick(
            index: Int,
            count: Int,
            level: Level,
            cyEntity: CE
        ) where CE : Entity, CE : ICypherEntity {
            val target = cyEntity.hooksSharedData.homingTarget ?: return
            if (!target.boundingBox.contains(cyEntity.position())) {
                val dir = cyEntity.position().vectorTo(target.eyePosition)
                val dis =  min(dir.length(), count * HOMING_STRENGTH_LEVEL + HOMING_STRENGTH)
                val speed = dir.normalize().scale(dis)
                cyEntity.addDeltaMovement(speed)
            }
        }
    }

    class TurnTowardTarget(defaultAttribute: Builder.() -> Builder) : AbstractTargetHoming("turn_toward_target", defaultAttribute) {
        override fun <CE> onTick(
            index: Int,
            count: Int,
            level: Level,
            cyEntity: CE
        ) where CE : Entity, CE : ICypherEntity {
            val target = cyEntity.hooksSharedData.homingTarget ?: return
            if (!target.boundingBox.contains(cyEntity.position())) {
                val dir = cyEntity.position().vectorTo(target.eyePosition)
                cyEntity.deltaMovement = cyEntity.deltaMovement.rotateTowards(dir, ROTATION_RADIUS * count)
            }
        }
    }
}