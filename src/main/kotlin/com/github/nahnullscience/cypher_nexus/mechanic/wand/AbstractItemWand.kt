package com.github.nahnullscience.cypher_nexus.mechanic.wand

import com.github.nahnullscience.cypher_nexus.init.ModDataAttachments.WAND_DATA_MAP
import com.github.nahnullscience.cypher_nexus.init.ModDataComponents
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.HelperDataBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingState
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileStateChunk
import com.github.nahnullscience.cypher_nexus.mechanic.event.CNEvents
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.ItemWandInstance
import com.github.nahnullscience.cypher_nexus.network.client.ClientboundSyncWandInstance
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

/**
 * item wand, its duty is as follows:
 * 1. hold wand data (data-component on stack)
 * 2. perform animation
 * */
abstract class AbstractItemWand(
    properties: Properties = Properties()
) : Item(
    properties.stacksTo(1)
), IWandLike  {
    abstract override val isEditableWand: Boolean

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
        val wandData = getWandData(stack)

        val instance = invoker.getData(WAND_DATA_MAP).getOrPutInstance(wandData, this, invoker.level())
        val useTime = getUseDuration(stack, invoker) - remainingUseDuration
        if (invoker.level().gameTime - useTime >= instance.lastInvokeTime) return  // stop sync if no conduction performed

        instance.sendSyncStatePacket(invoker)
    }


    override fun getUseDuration(stack: ItemStack, entity: LivingEntity) = 72000
    override fun onUseTick(level: Level, invoker: LivingEntity, stack: ItemStack, remainingUseDuration: Int) {
        //println(remainingUseDuration) // #getUseDuration() - used ticks, resets to full if reach 0

        // will call on both sides
        tryInvoke(level, invoker, stack)
    }


    override fun inventoryTick(stack: ItemStack, level: ServerLevel, entity: Entity, slot: EquipmentSlot?) {
        // call through (living#aistep)EntityEquipment#tick / (player#aistep)Inventory#tick -> stack#tick -> item#tick
        // inventory 0-35, does not contain equipment-slots, so everything tick once per tick

        if (entity is Player) return // tick player hotbar through events, since that runs on both sides
        if (slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) {
//            val dataInstance = entity.getData(WAND_DATA_MAP)
//                .getOrPutInstance(getWandData(stack, entity) ?: return, this, level)
//            dataInstance.tick(entity)
            itemWandInstance(level, entity, stack).tick(entity)
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected fun getWandData(stack: ItemStack) = getWandData(stack, null)
    override fun <EntityWand> getWandData(
        stack: ItemStack?,
        entityWand: EntityWand?
    ): WandDataBundle where EntityWand : Entity, EntityWand : IWandLike {
        /* @doc
         * Any component values within the map should be treated as immutable.
         * Always call #set or one of its referring methods discussed below after modifying the value of a data component.
         * */
        run {
            if (stack != null && !stack.isEmpty) {
                val invariable = stack.get(ModDataComponents.WAND_INVARIABLE) ?: return@run
                val highPayload = stack.get(ModDataComponents.WAND_HIGH_PAYLOAD) ?: return@run

                return WandDataBundle(invariable, highPayload)
            }
        }

        return WandDataBundle.missingData { "wand [$stack] missing data" }
    }


    override fun checkInvokingPrerequisites(level: Level, invoker: Entity, stack: ItemStack?): Boolean {
        return itemWandInstance(level, invoker, stack).canInvoke()
    }

    // for an Item Wand, pos and dire just use the living's view vector
    override fun getInvokePosDire(level: Level, invoker: Entity, wandLength: Float): PosDirePair {
        val dire = invoker.lookAngle
        val pos = invoker.eyePosition.add(dire.scale(wandLength.toDouble()))
        return PosDirePair(pos, dire)
    }


    override fun itemWandInstance(level: Level, invoker: Entity, stack: ItemStack?): ItemWandInstance {
        val wandData = getWandData(stack, null)
        return invoker.getData(WAND_DATA_MAP).getOrPutInstance(wandData, this, level)
    }


    override fun getHelperDataBundle(level: Level, invoker: Entity, stack: ItemStack?): HelperDataBundle {
        return itemWandInstance(level, invoker, stack).toHelperDataBundle()
    }


    override fun afterInvoke(
        level: Level,
        invoker: Entity,
        stack: ItemStack?,
        dataBundle: HelperDataBundle,
        rootChunk: ProjectileStateChunk
    ): InvokingState {
        val instance = itemWandInstance(level, invoker, stack)
        instance.updateFromHelperData(dataBundle)
        instance.invokeFinish(level)
        return InvokingState.SUCCESS
    }
}