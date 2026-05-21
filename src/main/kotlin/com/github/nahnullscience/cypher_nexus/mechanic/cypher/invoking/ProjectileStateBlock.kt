package com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttribute
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.CypherAttributeOperation
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookContainer
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.hook.HookModule
import com.github.nahnullscience.cypher_nexus.utility.i.IFlaggable
import com.github.nahnullscience.cypher_nexus.utility.mod.PosDirePair
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.Level

class ProjectileStateBlock() : IFlaggable {
    override var enabledFlags: Int = 0
    val computedOperationMap = HashMap<CypherAttribute, HashMap<CypherAttributeOperation, Double>>()
    val invokeHookContainer = HookContainer(HookModule.HookType.INVOKING)
    val projectileHookContainer = HookContainer(HookModule.HookType.PROJECTILE)

    //val modifiers = mutableListOf<ModifierNode>()
    val projectiles = mutableListOf<ProjectileNode>()

    fun release(level: Level, invoker: LivingEntity?, posDire: PosDirePair) {
        for (p in projectiles) {
            val proj = p.instance.createProjectile(level, invoker, posDire.position, posDire.direction, this, p.payload)
            level.addFreshEntity(proj)
        }
    }
}