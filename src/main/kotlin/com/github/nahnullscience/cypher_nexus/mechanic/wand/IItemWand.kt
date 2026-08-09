package com.github.nahnullscience.cypher_nexus.mechanic.wand

import com.github.nahnullscience.cypher_nexus.init.ModDataComponents
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.HelperDataBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingState
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.ItemWandDataInvariable
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.ItemWandInstance
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.WandDataHighPayload
import com.github.nahnullscience.cypher_nexus.utility.CoordinateDefinition
import com.github.nahnullscience.cypher_nexus.utility.PosDirePair
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
     * determine the initial position & direction of the projectile,
     * will be further processed if hooks are present.
     * direction doesn't have to be normalized
     * */
    fun getInvokingPosDire(level: Level, invoker: Entity, coordinate: CoordinateDefinition, stack: ItemStack): PosDirePair

    /**
     * resolve invoking feedback, for item-wands this is handled by [ItemWandInstance]
     * */
    fun afterInvoke(level: Level, invoker: Entity, stack: ItemStack, dataBundle: HelperDataBundle, rootChunk: ShotStateChunk): InvokingState

    /**
     * call on BOTH sides.
     * server side is responsible for projectile generation, authorise mana / deck / delay check.
     * client side is for user info overlay, and wand module functions.
     * */
    override fun tryInvoke(level: Level, invoker: Entity, coordinate: CoordinateDefinition, dataProvider: ItemStack): InvokingState {
        val instance = getWandInstance(level, invoker, dataProvider)
        if (!instance.canInvoke()) return InvokingState.LOADING

        val aoc = getInvokingRecipe(dataProvider)
        val state = instance.toHelperDataBundle()
        val posDire = getInvokingPosDire(level, invoker, coordinate, dataProvider)

        val helper = InvokingHelper(aoc, state, invoker = invoker)

        helper.processSync()
//        scope.launch { // TODO if async #checkInvokingPrerequisites should handle "pending" state
//            helper.process()
//            helper.finalizeInvoking(
//                level,
//                getInvokePosDire(level, invoker),
//                itemWandInstance(level, invoker, stack)
//            )
//            afterInvoke(level, invoker, stack, data, helper.rootChunk)
//            return InvokingState.HANG
//        }

        helper.releaseInvokingResult(level, coordinate, posDire, instance)
        return afterInvoke(level, invoker, dataProvider, state, helper.shotRoot)
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