package com.github.nahnullscience.cypher_nexus.mechanic.wand

import com.github.nahnullscience.cypher_nexus.init.ModDataAttachments.WAND_DATA_MAP
import com.github.nahnullscience.cypher_nexus.init.ModDataComponents
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.event.CNEvents
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.WandDataHighPayload
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.WandInstance
import com.github.nahnullscience.cypher_nexus.network.client.ClientboundSyncWandInstance
import com.github.nahnullscience.cypher_nexus.utility.mod.ArrayOfCyphers
import com.github.nahnullscience.cypher_nexus.utility.mod.PosDirePair
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.ItemUseAnimation
import net.minecraft.world.level.Level
import net.neoforged.neoforge.network.PacketDistributor

abstract class AbstractItemWand(
    properties: Properties = Properties()
) : Item(
    properties.stacksTo(1)
), IWandLike  {
    abstract override val isEditableWand: Boolean
    fun instance(stack: ItemStack, level: Level, invoker: Entity) : WandInstance? {
        val wandData = getWandData(stack, invoker) ?: return null
        return if (invoker.hasData(WAND_DATA_MAP)) invoker.getData(WAND_DATA_MAP)[wandData.invariable.uuid]
        else null
    }

    override fun getUseAnimation(stack: ItemStack) = ItemUseAnimation.SPYGLASS
    override fun use(level: Level, player: Player, usedHand: InteractionHand): InteractionResult {
        val stack = player.getItemInHand(usedHand)
        val f = CNEvents.canConductWand(player, stack, usedHand, level)
        if (f) {
            player.startUsingItem(usedHand)
            return InteractionResult.CONSUME
        }
        return InteractionResult.FAIL
    }

    override fun onStopUsing(stack: ItemStack, invoker: LivingEntity, remainingUseDuration: Int) {
        // println("releaseUsing $level") // call on both sides
        if (invoker !is ServerPlayer) return
        val wandData = getWandData(stack, invoker) ?: return

        val instance = invoker.getData(WAND_DATA_MAP).getOrPutInstance(wandData, this, invoker.level())
        val useTime = getUseDuration(stack, invoker) - remainingUseDuration
        if (invoker.level().gameTime - useTime >= instance.lastInvokeTime) return  // stop sync if no conduction performed

        // FIXME this causes client delay / recharge bar flash, try sync somewhere else
        val helperBundle = instance.toHelperDataBundle()
        PacketDistributor.sendToPlayer(
            invoker,
            ClientboundSyncWandInstance(
                wandData.invariable.uuid,
                helperBundle.manaCurrent,
                helperBundle.delay,
                helperBundle.recharge,
                helperBundle.deck
            )
        )
    }


    override fun getUseDuration(stack: ItemStack, entity: LivingEntity) = 72000
    override fun onUseTick(level: Level, invoker: LivingEntity, stack: ItemStack, remainingUseDuration: Int) {
        //println(remainingUseDuration) // #getUseDuration() - used ticks, resets to full if reach 0

        // will call on both sides
        tryConduct(level, invoker, stack)
    }


    override fun inventoryTick(stack: ItemStack, level: ServerLevel, entity: Entity, slot: EquipmentSlot?) {
        // call through (living#aistep)EntityEquipment#tick / (player#aistep)Inventory#tick -> stack#tick -> item#tick
        // inventory 0-35, does not contain equipment-slots, so everything tick once per tick

        if (entity is Player) return // tick player hotbar through events, since that runs on both sides
        if (slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) {
            val dataInstance = entity.getData(WAND_DATA_MAP)
                .getOrPutInstance(getWandData(stack, entity) ?: return, this, level)
            dataInstance.tick(entity)
        }
    }

    override fun getWandData(stack: ItemStack?, invoker: Entity?): WandDataBundle? {
        /* @doc
         * Any component values within the map should be treated as immutable.
         * Always call #set or one of its referring methods discussed below after modifying the value of a data component.
         * */
        if (stack != null && !stack.isEmpty) {
            val invariable = stack.get(ModDataComponents.WAND_INVARIABLE) ?: return null
            val highPayload = stack.get(ModDataComponents.WAND_HIGH_PAYLOAD) ?: return null

            return WandDataBundle(invariable, highPayload)
        }
        return null
    }

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