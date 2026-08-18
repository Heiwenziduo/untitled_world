package com.github.nahnullscience.cypher_nexus.mechanic.wand

import com.github.nahnullscience.cypher_nexus.init.ModDataComponents
import com.github.nahnullscience.cypher_nexus.init.mod.CypherAttributes
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.attribute.AttributeOperator
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.HelperDataBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingResult
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotState
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.ItemWandDataInvariable
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.ItemWandInstance
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.WandDataHighPayload
import com.github.nahnullscience.cypher_nexus.utility.linear_space.AnchoredCoordinate
import com.github.nahnullscience.cypher_nexus.utility.mod.ArrayOfCyphers
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level


interface IItemWand : IWandLike<ItemStack> {

    /** for item implementations to determine whether it can be modified in cypher-index */
    val isEditableWand: Boolean

    override fun getWandData(dataProvider: ItemStack): ItemWandDataInvariable

    override fun getInvokingRecipe(dataProvider: ItemStack): ArrayOfCyphers

    /***/
    @Deprecated("")
    override fun getWandState(level: Level, invoker: Entity, dataProvider: ItemStack): HelperDataBundle {
        return getWandInstance(level, invoker, dataProvider).toHelperDataBundle()
    }

    /**  */
    fun getWandInstance(level: Level, invoker: Entity, stack: ItemStack): ItemWandInstance

    /**
     * a window for item-wands to process coordinate provided by `input-module`.
     *
     * use this to adjust the initial position & direction of projectiles.
     *
     * the result may be further processed if hooks are present.
     * */
    fun adjustInvokingCoordinate(level: Level, invoker: Entity, coordinate: AnchoredCoordinate, stack: ItemStack)

    /**
     * resolve invoking feedback, for item-wands this is handled by [ItemWandInstance]
     * */
    fun afterInvoke(
        level: Level,
        invoker: Entity,
        coordinate: AnchoredCoordinate,
        stack: ItemStack,
        dataBundle: HelperDataBundle,
        shotStateRoot: ShotState
    ): InvokingResult

    /**
     * call on BOTH sides.
     * server side is responsible for projectile generation, authorise mana / deck / delay check.
     * client side is for user info overlay, and wand module functions.
     * */
    override fun tryInvoke(
        level: Level,
        invoker: Entity,
        coordinate: AnchoredCoordinate,
        dataProvider: ItemStack
    ): InvokingResult {
        val instance = getWandInstance(level, invoker, dataProvider)
        if (!instance.canInvoke()) return InvokingResult.LOADING

        val aoc = getInvokingRecipe(dataProvider)
        val state = instance.toHelperDataBundle()

        val helper = InvokingHelper(aoc, state, invoker = invoker)

        run attr@ {
            val shotAttr = helper.shotRoot.accessor
            shotAttr.addRaw(CypherAttributes.SPREAD, AttributeOperator.ADD, instance.wandData.chunkF.spread.toDouble())
        }

        helper.processSync()
        // TODO if async #checkInvokingPrerequisites should handle "pending" state
//        scope.launch {
//            helper.process()
//            helper.finalizeInvoking(
//                level,
//                getInvokePosDire(level, invoker),
//                itemWandInstance(level, invoker, stack)
//            )
//            afterInvoke(level, invoker, stack, data, helper.rootChunk)
//            return InvokingState.HANG
//        }

        adjustInvokingCoordinate(level, invoker, coordinate, dataProvider)
        helper.shotRoot.release(level, coordinate, invoker, invoker)
        // helper.releaseInvokingResult(level, coordinate)
        return afterInvoke(level, invoker, coordinate, dataProvider, state, helper.shotRoot)
    }



    companion object {
//        private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
//        private val scope = CoroutineScope(Dispatchers.Default)

        /**
         * @return whether the item behind the stack is a decent [IItemWand]
         * */
        fun ItemStack.isWand(): Boolean = !isEmpty && item is IItemWand
        fun ItemStack.isNotWand(): Boolean = !isWand()

        fun ItemStack.getWandData(): ItemWandDataInvariable? {
            return (this.item as? IItemWand)?.getWandData(this)
        }

        fun ItemStack.getInvokingRecipe(): ArrayOfCyphers? {
            return (this.item as? IItemWand)?.getInvokingRecipe(this)
        }

        /**
         * handle data components
         * @return true if edit success
         * */
        fun ItemStack.editRecipeIfWand(aoc: ArrayOfCyphers): Boolean {
            if (isWand()) {
                println("editWand: $this")
                set(ModDataComponents.WAND_HIGH_PAYLOAD, WandDataHighPayload(aoc))
                return true
            } else return false
        }

        fun ItemStack.wandInstanceOrNull(invoker: Entity): ItemWandInstance? =
            if (isEmpty) null
            else (item as? IItemWand)?.getWandInstance(invoker.level(), invoker, this)
    }
}