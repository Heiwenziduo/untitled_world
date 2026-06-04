package com.github.nahnullscience.cypher_nexus.mechanic.wand

import com.github.nahnullscience.cypher_nexus.init.ModDataAttachments.WAND_DATA_MAP
import com.github.nahnullscience.cypher_nexus.init.ModDataComponents
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.event.CNEvents
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.WandDataHighPayload
import com.github.nahnullscience.cypher_nexus.utility.mod.ArrayOfCyphers
import com.github.nahnullscience.cypher_nexus.utility.mod.PosDirePair
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.UseAnim
import net.minecraft.world.level.Level

abstract class AbstractItemWand(
    properties: Properties = Properties()
) : Item(
    properties.stacksTo(1)
), IWandLike  {
    abstract override val isEditableWand: Boolean
    override fun getUseAnimation(stack: ItemStack) = UseAnim.CUSTOM
    override fun use(level: Level, player: Player, usedHand: InteractionHand): InteractionResultHolder<ItemStack> {
        val stack = player.getItemInHand(usedHand)
        val f = CNEvents.canConductWand(player, stack, usedHand, level)
        if (f) {
            player.startUsingItem(usedHand)
            return InteractionResultHolder.consume(stack)
        }
        return InteractionResultHolder.fail(stack)
    }

    override fun releaseUsing(stack: ItemStack, level: Level, livingEntity: LivingEntity, timeCharged: Int) {}

//    override fun useOnRelease(stack: ItemStack): Boolean {
//        return super.useOnRelease(stack)
//    }

    override fun getUseDuration(stack: ItemStack, entity: LivingEntity) = 100
    override fun onUseTick(level: Level, invoker: LivingEntity, stack: ItemStack, remainingUseDuration: Int) {
        // will call on both sides
        if (!level.isClientSide) {
            tryConduct(level, invoker, stack)
        }
        //println(remainingUseDuration) // #getUseDuration() - used ticks, resets to full if reach 0
    }


    override fun inventoryTick(stack: ItemStack, level: Level, entity: Entity, slotId: Int, isSelected: Boolean) {
        //if (level.isClientSide) return

        // Mob do not implement Container, but can access its inventory through the seven EquipmentSlot enum values:
        // MAINHAND, OFFHAND, FEET, LEGS, CHEST, HEAD, and BODY (where BODY is used for horse and dog armor).
        // entity is Mob && entity.getItemBySlot()
        if (entity is Player && (slotId in 0..8 || slotId == Inventory.SLOT_OFFHAND)) { // nine hotbar slots (indices 0-8).
            val dataInstance = entity.getData(WAND_DATA_MAP)
                .getOrPutInstance(getWandData(stack, entity) ?: return, this, level)

            dataInstance.tick(entity)
        }
    }

    override fun getWandData(stack: ItemStack?, caster: Entity?): WandDataBundle? {
        if (stack != null && !stack.isEmpty) {
            val invariable = stack.get(ModDataComponents.WAND_INVARIABLE) ?: return null
            val highPayload = stack.get(ModDataComponents.WAND_HIGH_PAYLOAD) ?: return null

            return WandDataBundle(invariable, highPayload)
        }
        return null
    }

//    override fun setWandData(
//        stack: ItemStack?,
//        invariable: WandDataInvariable?,
//        highPayload: WandDataHighPayload?,
//        frequent: WandDataFrequent
//    ) {
//        /* @doc
//         * Any component values within the map should be treated as immutable.
//         * Always call #set or one of its referring methods discussed below after modifying the value of a data component.
//         * */
//
//        stack?.set(ModDataComponents.WAND_FREQUENT, frequent)
//    }

    /** for an Item Wand, pos and dire just use the living's view vector */
    override fun getInvokePosDire(level: Level, invoker: Entity, wandLength: Float): PosDirePair {
        val dire = invoker.lookAngle
        val pos = invoker.eyePosition.add(dire.scale(wandLength.toDouble()))
        return PosDirePair(pos, dire)
    }


    companion object {
        // TODO check data authentic
        fun editWand(stack: ItemStack, list: List<AbstractCypher>) {
            println("editWand: $stack")
            // TODO maybe we should use AbstractItemWand instead?
            if (stack.item is IWandLike) {
                stack.set(ModDataComponents.WAND_HIGH_PAYLOAD, WandDataHighPayload(ArrayOfCyphers(list)))
            }
        }
//        fun resetIndex(stack: ItemStack) {
//            println("resetIndex: $stack")
//            if (stack.item is IWandLike) {
//                val fre = stack.get(ModDataComponents.WAND_FREQUENT)
//                if (fre != null) {
//                    stack.set(ModDataComponents.WAND_FREQUENT, fre.fromStart())
//                }
//            }
//        }
    }
}