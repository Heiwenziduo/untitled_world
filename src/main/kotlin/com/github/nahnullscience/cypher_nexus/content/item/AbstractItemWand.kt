package com.github.nahnullscience.cypher_nexus.content.item

import com.github.nahnullscience.cypher_nexus.init.ModDataComponents
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.wand.IWandLike
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.WandDataFrequent
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.WandDataHighPayload
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.WandDataInvariable
import com.github.nahnullscience.cypher_nexus.utility.mod.ArrayOfCyphers
import com.github.nahnullscience.cypher_nexus.utility.mod.PosDirePair
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Inventory.SLOT_OFFHAND
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import kotlin.math.min

abstract class AbstractItemWand : Item(
    Properties()
        .stacksTo(1)
        .component(ModDataComponents.WAND_INVARIABLE, WandDataInvariable.Companion.DEFAULT)
        .component(ModDataComponents.WAND_HIGH_PAYLOAD, WandDataHighPayload.Companion.DEFAULT)
        .component(ModDataComponents.WAND_FREQUENT, WandDataFrequent.Companion.DEFAULT)
), IWandLike  {
    abstract override val isEditableWand: Boolean
    override fun use(level: Level, player: Player, usedHand: InteractionHand): InteractionResultHolder<ItemStack> {
        val stack = player.getItemInHand(usedHand)

        // TODO
        tryConduct(level, player, stack)

        return InteractionResultHolder.success(stack)
    }

    override fun inventoryTick(stack: ItemStack, level: Level, entity: Entity, slotId: Int, isSelected: Boolean) {
        if (level.isClientSide) return
        // Mob do not implement Container, but can access its inventory through the seven EquipmentSlot enum values:
        // MAINHAND, OFFHAND, FEET, LEGS, CHEST, HEAD, and BODY (where BODY is used for horse and dog armor).
        // entity is Mob && entity.getItemBySlot()

        if (entity is Player && (slotId in 0..8 || slotId == SLOT_OFFHAND)) { // nine hotbar slots (indices 0-8).
            wandTick(stack, entity)
        }
    }

    protected fun wandTick(stack: ItemStack, player: Player) {
        val (invariable, highPayload, frequent) = getWandData(stack, player)?: return
        var flag = false
        val (maxMana, manaRegen) = invariable.chunkF
        var (manaCurrent, index, delay, recharge, ) = frequent
        if (manaCurrent < maxMana) {
            manaCurrent = min(manaCurrent + manaRegen, maxMana)
            flag = true
//            println("mana regen -> $manaCurrent")
        }
        if (delay > 0) {
            delay--
            flag = true
        }
        if (index == 0 && recharge > 0) {
            recharge--
            flag = true
        }

        // FIXME use timestamp instead of checking every tick, which may lead to massive network pressure
        if (flag) stack.set(ModDataComponents.WAND_FREQUENT,
            WandDataFrequent(manaCurrent, index, delay, recharge, frequent.deck, frequent.discard))
    }


    override fun getWandData(stack: ItemStack?, caster: LivingEntity?): IWandLike.WandDataBundle? {
        if (stack != null && !stack.isEmpty) {
            val invariable = stack.get(ModDataComponents.WAND_INVARIABLE)
            val highPayload = stack.get(ModDataComponents.WAND_HIGH_PAYLOAD)
            val frequent = stack.get(ModDataComponents.WAND_FREQUENT)

            if (invariable != null && highPayload != null && frequent != null)
                return IWandLike.WandDataBundle(invariable, highPayload, frequent)
        }
        return null
    }

    override fun setWandData(
        stack: ItemStack?,
        invariable: WandDataInvariable?,
        highPayload: WandDataHighPayload?,
        frequent: WandDataFrequent
    ) {
        /* @doc
         * Any component values within the map should be treated as immutable.
         * Always call #set or one of its referring methods discussed below after modifying the value of a data component.
         * */

        //if (invariable != null)
        stack?.set(ModDataComponents.WAND_FREQUENT, frequent)
    }

    /** for an Item Wand, pos and dire just use the living's view vector */
    override fun getInvokePosDire(level: Level, invoker: LivingEntity, wandLength: Float): PosDirePair {
        val dire = invoker.lookAngle
        val pos = invoker.eyePosition.add(dire.scale(wandLength.toDouble()))
        return PosDirePair(pos, dire)
    }


    companion object {
        // TODO check data authentic
        fun editWand(stack: ItemStack, list: List<AbstractCypher>) {
            println("editWand: $stack")
            // TODO maybe we should use BasicWandItem instead?
            if (stack.item is IWandLike) {
                stack.set(ModDataComponents.WAND_HIGH_PAYLOAD, WandDataHighPayload(ArrayOfCyphers(list)))
            }
        }
        fun resetIndex(stack: ItemStack) {
            println("resetIndex: $stack")
            if (stack.item is IWandLike) {
                val fre = stack.get(ModDataComponents.WAND_FREQUENT)
                if (fre != null) {
                    stack.set(ModDataComponents.WAND_FREQUENT, fre.fromStart())
                }
            }
        }
    }
}

