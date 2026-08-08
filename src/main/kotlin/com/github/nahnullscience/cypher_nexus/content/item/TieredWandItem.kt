package com.github.nahnullscience.cypher_nexus.content.item

import com.github.nahnullscience.cypher_nexus.init.ModDataComponents.WAND_HIGH_PAYLOAD
import com.github.nahnullscience.cypher_nexus.init.ModDataComponents.WAND_INVARIABLE
import com.github.nahnullscience.cypher_nexus.mechanic.wand.AbstractItemWand
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.ItemWandDataInvariable
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.WandDataHighPayload
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.inventory.tooltip.TooltipComponent
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.ItemUseAnimation
import java.util.*

/**
 *
 * */
open class TieredWandItem(
    property: Item.Properties
) : AbstractItemWand(
    property
) {
    override val isEditableWand = true

    override fun getUseAnimation(stack: ItemStack): ItemUseAnimation = ItemUseAnimation.DRINK

    override fun inventoryTick(stack: ItemStack, level: ServerLevel, entity: Entity, slot: EquipmentSlot?) {
//        val i = stack.get(WAND_INVARIABLE) ?: run {
//            val d = ItemWandDataInvariable.default()
//            stack.set(WAND_INVARIABLE, d)
//            println("gen invariable data: ${d.uuid}")
//            d
//        }
//        val h = stack.get(WAND_HIGH_PAYLOAD) ?: run {
//            val d = WandDataHighPayload.of(15)
//            stack.set(WAND_HIGH_PAYLOAD, d)
//            println("gen aoc data: ${d.aoc}")
//            d
//        }
        super.inventoryTick(stack, level, entity, slot)
    }

    override fun generateWandData(stack: ItemStack) {
        val i = ItemWandDataInvariable.default()
        val a = WandDataHighPayload.of(15)
        stack.set(WAND_INVARIABLE, i)
        stack.set(WAND_HIGH_PAYLOAD, a)
    }


    override fun getDefaultInstance(): ItemStack {
        return super.getDefaultInstance()
    }



    override fun getName(itemStack: ItemStack): Component {
        return super.getName(itemStack)
//        val uu = getWandData(itemStack)?.invariable?.uuid.toString()
//        return Component.empty()
//            .append(CommonComponents.TRANSFER_CONNECT_FAILED)
//            .append(CommonComponents.NEW_LINE)
//            .append(CommonComponents.GUI_DISCONNECT)
//            .append(CommonComponents.SPACE).append(CommonComponents.CONNECT_FAILED)
//            .append(uu)
    }

    override fun getTooltipImage(itemStack: ItemStack): Optional<TooltipComponent> {
        return super.getTooltipImage(itemStack)
    }
}