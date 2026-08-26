package com.github.nahnullscience.cypher_nexus.content.cypher.modifier

import com.github.nahnullscience.cypher_nexus.CypherNexus
import com.github.nahnullscience.cypher_nexus.init.ModDataAttachments
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherDataMap.Builder
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.ModifierCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.entity.components.ICypherEntity
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.FirstTickHook
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.projectile.GeneralOnHitHook
import com.github.nahnullscience.cypher_nexus.mechanic.entity.collision.CETargetStorageGridsManager.Companion.forEachEntityRayCast
import com.github.nahnullscience.cypher_nexus.utility.isServerSide
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.animal.golem.IronGolem
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.arrow.Arrow
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.HitResult.Type
import kotlin.math.max

class FieryCypher(
    defaultAttribute: Builder.() -> Builder
) : ModifierCypher(defaultAttribute), GeneralOnHitHook
, FirstTickHook
{
    override val resource = CypherNexus.modResource("fiery")
    override fun <CE> onHit(
        index: Int,
        count: Int,
        level: Level,
        cyEntity: CE,
        result: HitResult
    ) where CE : Entity, CE : ICypherEntity {
        if (result is EntityHitResult && result.type != Type.MISS) {
            val target = result.entity
            target.remainingFireTicks = max(target.remainingFireTicks, 200)
        }
    }

    // test
    override fun <CE> onFirstTick(
        index: Int,
        count: Int,
        level: Level,
        cyEntity: CE
    ) where CE : Entity, CE : ICypherEntity {
        run test@ {
//            val manager = level.getData(ModDataAttachments.STORAGE_GRID_MANAGER)
//            val pos = cyEntity.position()
//            val dir = cyEntity.deltaMovement
//            val level = cyEntity.level()
//            repeat(10000) {
//                manager.forEachEntityRayCast(pos, dir, 0.3) { entity, t, direction ->
//                    if (level.isServerSide && entity is LivingEntity && entity !is IronGolem && entity !is Player) {
//                        entity.health -= 1f
//                    }
//                }
//            }
        }
        if (level is ServerLevel) {
//            val pos = cyEntity.position()
//            val speed = cyEntity.deltaMovement
//            val dirt = Items.ARROW.defaultInstance
//            for (i in 0 until 100) {
//                val arrow = Arrow(level, pos.x, pos.y, pos.z, dirt, null)
//                arrow.shoot(speed.x, speed.y, speed.z, 0.5f, 0.5f)
//                level.addFreshEntity(arrow)
//            }
        }
    }
}
