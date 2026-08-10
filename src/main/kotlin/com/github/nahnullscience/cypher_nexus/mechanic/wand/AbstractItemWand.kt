package com.github.nahnullscience.cypher_nexus.mechanic.wand

import com.github.nahnullscience.cypher_nexus.init.ModDataAttachments.WAND_DATA_MAP
import com.github.nahnullscience.cypher_nexus.init.ModDataComponents
import com.github.nahnullscience.cypher_nexus.init.mod.WandModuleTypes.RECOIL_MODULE
import com.github.nahnullscience.cypher_nexus.init.mod.WandModuleTypes.SECONDARY_MODULE
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingHelper.HelperDataBundle
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.InvokingState
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.invoking.ShotStateChunk
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.ItemWandDataInvariable
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.ItemWandDataInvariable.Companion.FALL_BACK
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.ItemWandDataInvariable.Companion.TO_BE_GENERATED
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.ItemWandInstance
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.WandDataHighPayload.Companion.EMPTY
import com.github.nahnullscience.cypher_nexus.utility.CoordinateDefinition
import com.github.nahnullscience.cypher_nexus.utility.PosDirePair
import com.github.nahnullscience.cypher_nexus.utility.mod.ArrayOfCyphers
import com.github.nahnullscience.cypher_nexus.utility.nearestHitPoint
import net.minecraft.core.component.DataComponentMap
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.item.ItemEntity
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
), IItemWand  {
    abstract override val isEditableWand: Boolean

    override fun getUseAnimation(stack: ItemStack) = ItemUseAnimation.EAT
    override fun getUseDuration(stack: ItemStack, entity: LivingEntity) = 114

    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResult {
        val stack = player.getItemInHand(hand)
        val instance = getWandInstance(level, player, stack)

        val module = instance.getModule(SECONDARY_MODULE) ?: return InteractionResult.PASS
        // if not startUsingItem, further functions like onUseTick / onStopUsing will not perform
        // player.startUsingItem(hand)
        return module.onVanillaUseStart(player, stack, hand)
    }

    override fun onUseTick(level: Level, user: LivingEntity, stack: ItemStack, remainTicks: Int) {
        // call on both sides
        val instance = getWandInstance(level, user, stack)
        instance.getModule(SECONDARY_MODULE)?.onVanillaUseTick(user, stack, remainTicks) // #getUseDuration - used ticks, resets to full if reach 0
    }

    override fun onStopUsing(stack: ItemStack, user: LivingEntity, remainTicks: Int) {
        // call on both sides
        val instance = getWandInstance(user.level(), user, stack)
        instance.getModule(SECONDARY_MODULE)?.onVanillaUseStop(user, stack, remainTicks)
    }

    override fun inventoryTick(stack: ItemStack, level: ServerLevel, entity: Entity, slot: EquipmentSlot?) {
        if (getWandData(stack) == TO_BE_GENERATED) {
            generateWandData(stack)
        }

        // call through (living#aistep)EntityEquipment#tick / (player#aistep)Inventory#tick -> stack#tick -> item#tick
        // inventory 0-35, does not contain equipment-slots, so everything tick once per tick

        if (entity is Player) return // tick player hotbar through events, since that runs on both sides
        if (slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) {
            getWandInstance(level, entity, stack).tick(entity)
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

    override fun onEntityItemUpdate(stack: ItemStack, entity: ItemEntity): Boolean {
        return super.onEntityItemUpdate(stack, entity)
    }

    override fun components(): DataComponentMap {
        return super.components()
    }

    override fun onCraftedPostProcess(itemStack: ItemStack, level: Level) {
        super.onCraftedPostProcess(itemStack, level)
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    protected abstract fun generateWandData(stack: ItemStack)

    override fun getWandData(dataProvider: ItemStack): ItemWandDataInvariable {
        return dataProvider.getOrDefault(ModDataComponents.WAND_INVARIABLE, FALL_BACK)
    }

    override fun getInvokingRecipe(dataProvider: ItemStack): ArrayOfCyphers {
        return dataProvider.getOrDefault(ModDataComponents.WAND_HIGH_PAYLOAD, EMPTY).aoc
    }

    override fun getWandInstance(level: Level, invoker: Entity, stack: ItemStack): ItemWandInstance {
        return invoker.getData(WAND_DATA_MAP).getOrPutInstance(level, stack, this)
    }

    protected open fun wandLength(stack: ItemStack): Float {
        val aoc = getInvokingRecipe(stack)
        return 0.4f + (aoc.capacity.toFloat() / 16).coerceAtMost(3.0f)
    }

    override fun getInvokingPosDire(level: Level, invoker: Entity, coordinate: CoordinateDefinition, stack: ItemStack): PosDirePair {
        // for an Item Wand, pos and dire just use the living's view vector
        val tip = wandLength(stack)
        val eye = invoker.eyePosition
        val front = coordinate.front
        val scale = tip + invoker.knownMovement.dot(front).coerceAtLeast(0.0) // solve inertia problem

        val pos = eye.add(front.scale(scale)).let {
            level.nearestHitPoint(eye, it, invoker, 0.3)
        }
        return PosDirePair(pos, front)
    }

    override fun afterInvoke(
        level: Level,
        invoker: Entity,
        coordinate: CoordinateDefinition,
        stack: ItemStack,
        dataBundle: HelperDataBundle,
        shotStateRoot: ShotStateChunk
    ): InvokingState {
        val instance = getWandInstance(level, invoker, stack)
        instance.updateFromHelperData(dataBundle)
        instance.invokeFinish(level)

        if (invoker is LivingEntity) {
            val recoil = shotStateRoot.computeRecoil()
            instance.functionModule(RECOIL_MODULE.get(), invoker, stack, coordinate, power = recoil)
        }
        return InvokingState.SUCCESS
    }
}
