package com.github.nahnullscience.cypher_nexus.mechanic.wand

import com.github.nahnullscience.cypher_nexus.init.ModDataAttachments.WAND_DATA_MAP
import com.github.nahnullscience.cypher_nexus.init.ModDataComponents
import com.github.nahnullscience.cypher_nexus.init.mod.WandModuleTypes.SECONDARY
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.HelperDataBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingState
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ProjectileStateChunk
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.ItemWandInstance
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.WandDataBundle
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

/**
 * item wand, its main duty is as follows:
 * 1. hold wand data through DataComponent
 * 2. point to the [ItemWandInstance] represent this wand
 * 3. perform animation (TODO)
 * */
abstract class AbstractItemWand(
    properties: Properties = Properties()
) : Item(
    properties.stacksTo(1)
), IWandLike  {
    abstract override val isEditableWand: Boolean

    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResult {
        val stack = player.getItemInHand(hand)
        val instance = itemWandInstance(level, player, stack)

        return instance.module(SECONDARY)?.onInteract(player, instance, hand) ?: run {
            // if not startUsingItem, further functions like onStopUsing will not perform
            player.startUsingItem(hand)
            InteractionResult.PASS
        }
    }

    override fun getUseAnimation(stack: ItemStack) = ItemUseAnimation.EAT
    override fun getUseDuration(stack: ItemStack, entity: LivingEntity) = 114_514
    override fun onUseTick(level: Level, user: LivingEntity, stack: ItemStack, remainTicks: Int) {
        // #getUseDuration() - used ticks, resets to full if reach 0
        // will call on both sides

        val instance = itemWandInstance(level, user, stack)
        instance.doSecondaryTick(level, user, stack, remainTicks)
    }

    override fun onStopUsing(stack: ItemStack, user: LivingEntity, remainTicks: Int) {
        // call on both sides
        if (user is ServerPlayer) {
            val instance = itemWandInstance(user.level(), user, stack)
            val useTime = getUseDuration(stack, user) - remainTicks
            if (user.level().gameTime - useTime >= instance.lastInvokeTime) return  // stop sync if no conduction performed
            instance.sendSyncStatePacket(user)
        }
    }

    override fun inventoryTick(stack: ItemStack, level: ServerLevel, entity: Entity, slot: EquipmentSlot?) {
        // call through (living#aistep)EntityEquipment#tick / (player#aistep)Inventory#tick -> stack#tick -> item#tick
        // inventory 0-35, does not contain equipment-slots, so everything tick once per tick

        // tick player hotbar through events, since that runs on both sides
        if (entity is Player) return
        if (slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) {
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
    override fun getInvokePosDire(level: Level, invoker: Entity, stack: ItemStack?): PosDirePair {
        val tip = run {
            getWandData(stack ?: return@run 0.8).invariable.chunkF.wandLength.toDouble()
        }
        val dire = invoker.lookAngle
        val pos = invoker.eyePosition.add(dire.scale(tip))
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