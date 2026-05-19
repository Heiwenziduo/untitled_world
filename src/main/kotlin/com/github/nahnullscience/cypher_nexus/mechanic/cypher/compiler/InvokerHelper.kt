package com.github.nahnullscience.cypher_nexus.mechanic.cypher.compiler

import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractProjectileCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.CypherInvokerHelper
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.WandDataFrequent
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.WandDataInvariable
import com.github.nahnullscience.cypher_nexus.utility.mod.PosDirePair
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

/** cypher chain compiler */
class InvokerHelper (
    val level: Level,
    val invoker: LivingEntity?,
    val stack: ItemStack?,

    val wandStats: WandDataInvariable,
    val cypherList: List<AbstractCypher>,
    val helperData: CypherInvokerHelper.HelperDataBundle,

    /** direction doesn't have to be normalized */
    val invokePosDire: PosDirePair,
) {
    data class ProjectileCastNode(
        val cypherInstance: AbstractProjectileCypher,
        var triggerPayload: CypherPayloadBlock? = null
    )
    class CypherPayloadBlock {
        // Modifiers have no payload state, so storing the singletons directly is fine
        val modifiers = mutableListOf<AbstractCypher>()
        // Projectiles need state, so we store the Wrapper Nodes
        val projectiles = mutableListOf<ProjectileCastNode>()
    }


    fun parseBlock(context: CompilationContext, initialDraws: Int): CypherPayloadBlock {
        val currentBlock = CypherPayloadBlock()
        var drawsRemaining = initialDraws

        while (drawsRemaining > 0) {
            val currentCypher = context.getNextCypher() ?: break // Stop if wand is empty or wrap limits hit

            // ... process cypher, decrement drawsRemaining if it's a projectile ...
        }

        return currentBlock
    }



    data class HelperDataBundle (
        var draw: Int,
        var index: Int,
        var delay: Int,
        var recharge: Int,
        var manaCurrent: Float,
    ) {
        constructor(draw: Int, data: WandDataFrequent) : this(draw, data.index, data.delay, data.recharge, data.manaCurrent)
        fun frequentData() = WandDataFrequent(manaCurrent, index, delay, recharge,)

        fun withDraw(draw: Int) = HelperDataBundle(draw, index, delay, recharge, manaCurrent)
    }
}