package com.github.nahnullscience.cypher_nexus.mechanic.wand

import com.github.nahnullscience.cypher_nexus.init.ModDataAttachments.WAND_DATA_MAP
import com.github.nahnullscience.cypher_nexus.init.ModDataComponents
import com.github.nahnullscience.cypher_nexus.init.mod.WandModuleTypes.SECONDARY_MODULE
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.HelperDataBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingState
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.ItemWandInstance
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.WandDataBundle
import com.github.nahnullscience.cypher_nexus.utility.PosDirePair
import com.github.nahnullscience.cypher_nexus.utility.nearestHitPoint
import net.minecraft.server.level.ServerLevel
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

    override fun getUseAnimation(stack: ItemStack) = ItemUseAnimation.EAT
    override fun getUseDuration(stack: ItemStack, entity: LivingEntity) = 114

    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResult {
        val stack = player.getItemInHand(hand)
        val instance = itemWandInstance(level, player, stack)

        val module = instance.getModule(SECONDARY_MODULE) ?: return InteractionResult.PASS
        // if not startUsingItem, further functions like onUseTick / onStopUsing will not perform
        // player.startUsingItem(hand)
        return module.onVanillaUseStart(player, stack, hand)
    }

    override fun onUseTick(level: Level, user: LivingEntity, stack: ItemStack, remainTicks: Int) {
        // call on both sides
        val instance = itemWandInstance(level, user, stack)
        instance.getModule(SECONDARY_MODULE)?.onVanillaUseTick(user, stack, remainTicks) // #getUseDuration - used ticks, resets to full if reach 0
    }

    override fun onStopUsing(stack: ItemStack, user: LivingEntity, remainTicks: Int) {
        // call on both sides
        val instance = itemWandInstance(user.level(), user, stack)
        instance.getModule(SECONDARY_MODULE)?.onVanillaUseStop(user, stack, remainTicks)
    }

    override fun inventoryTick(stack: ItemStack, level: ServerLevel, entity: Entity, slot: EquipmentSlot?) {
        // call through (living#aistep)EntityEquipment#tick / (player#aistep)Inventory#tick -> stack#tick -> item#tick
        // inventory 0-35, does not contain equipment-slots, so everything tick once per tick

        if (entity is Player) return // tick player hotbar through events, since that runs on both sides
        if (slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) {
            itemWandInstance(level, entity, stack).tick(entity)
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

//    override fun isBarVisible(stack: ItemStack): Boolean {
//        return super.isBarVisible(stack)
//    }
//    override fun getBarColor(stack: ItemStack): Int = 0xFF00FFFF.toInt()
//    override fun getBarWidth(stack: ItemStack): Int {
//        return super.getBarWidth(stack)
//    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected open fun getWandData(stack: ItemStack?) = stack?.let { getWandData(stack, null) }
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

    protected open fun wandLength(data: WandDataBundle?): Float =
        data?.let { 0.4f + (it.highPayload.aoc.capacity.toFloat() / 16).coerceAtMost(3.0f) } ?: 0.4f

    override fun getInvokingPosDire(level: Level, invoker: Entity, stack: ItemStack?): PosDirePair {
        // for an Item Wand, pos and dire just use the living's view vector
        val tip = wandLength(getWandData(stack))
        val eye = invoker.eyePosition
        val looking = invoker.headLookAngle
        val scale = tip + invoker.knownMovement.dot(looking).coerceAtLeast(0.0) // solve inertia problem
        val pos = eye.add(looking.scale(scale)).let {
            level.nearestHitPoint(eye, it, invoker, 0.3)
        }
        return PosDirePair(pos, looking)
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
        rootChunk: ShotStateChunk
    ): InvokingState {
        val instance = itemWandInstance(level, invoker, stack)
        instance.updateFromHelperData(dataBundle)
        instance.invokeFinish(level)
        return InvokingState.SUCCESS
    }
}