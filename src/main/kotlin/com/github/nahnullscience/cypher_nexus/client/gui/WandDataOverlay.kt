package com.github.nahnullscience.cypher_nexus.client.gui

import com.github.nahnullscience.cypher_nexus.init.ModDataAttachments.WAND_DATA_MAP
import com.github.nahnullscience.cypher_nexus.mechanic.wand.IWandLike
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.WandDataInstance
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.LayeredDraw
import net.minecraft.util.Mth
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.ItemStack

object WandDataOverlay : LayeredDraw.Layer {

    override fun render(
        guiGraphics: GuiGraphics,
        deltaTracker: DeltaTracker
    ) {
        val player = Minecraft.getInstance().player
        if (Minecraft.getInstance().options.hideGui || player == null || player.isSpectator) {
            return
        }
        // TODO wands at other slots
        val wandMain = player.getItemInHand(InteractionHand.MAIN_HAND)
        val wandOff = player.getItemInHand(InteractionHand.OFF_HAND)

        // in the future maybe we can show multiple wands data, combine #gatherWand Event
        listOf(wandMain, wandOff).withIndex().forEach { (i, stack) ->
            val wand = stack.item
            if (stack.isEmpty || wand !is IWandLike) return@forEach
            val wandData = wand.getWandData(stack, player) ?: return@forEach

            // since item is a wand, instance should already exist
            val instance = player.getData(WAND_DATA_MAP).getOrPutInstance(wandData, wand, player.level())

            renderWand(guiGraphics, i, instance, stack)
        }

    }

    private fun renderWand(guiGraphics: GuiGraphics, offset: Int, instance: WandDataInstance, wandSack: ItemStack) {
        val screenWidth = guiGraphics.guiWidth()
        val screenHeight = guiGraphics.guiHeight()

        val barWidth = 80 // maybe flexible with manaMax?
        val margin = 16
        val startX = screenWidth - barWidth - margin
        var startY = screenHeight - 16 * (offset + 1)

        guiGraphics.renderItem(wandSack, screenWidth - margin, startY - 2)

        // draw bars one by one
        val manaProgress = instance.manaRegenPercent()
        drawProgressBar(guiGraphics, startX, startY, barWidth, 5, manaProgress,
            0xFF00BFFF.toInt(),
            0xFF00FFFF.toInt(),
            0.5f)
        startY += 8 // Move down for the next bar

        val castDelayProgress = instance.delayPercent()
        if (castDelayProgress < 1f) {
            drawProgressBar(guiGraphics, startX, startY, barWidth, 3, castDelayProgress,
                0xFF228B22.toInt(),
                0xFF32CD32.toInt(),
                0.5f)
        }

        if (instance.isRecharging) {
            val rechargeProgress = instance.rechargePercent()
            if (rechargeProgress < 1f) {
                drawProgressBar(guiGraphics, startX, startY, barWidth, 3, rechargeProgress,
                    0xFFFF8C00.toInt(),
                    0xFFFFD700.toInt(),
                    0.5f)
            }
        }

    }

    private fun drawProgressBar(
        guiGraphics: GuiGraphics,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        progress: Float,
        colorStart: Int,
        colorEnd: Int,
        bgAlphaMulti: Float
    ) {
        val borderColor = 0xFF111111.toInt()
        val bgBaseAlpha = (170 * bgAlphaMulti).toInt() shl 24 // Calculate dynamic alpha
        val bgColor = bgBaseAlpha or 0x000000 // Apply alpha to black background

        // Draw 1-pixel border outline
        guiGraphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, borderColor)

        // Draw background
        guiGraphics.fill(x, y, x + width, y + height, bgColor)

        val safeProgress = progress.coerceIn(0f, 1f)
        val fillWidth = (width * safeProgress).toInt()

        if (fillWidth > 0) {
            // Extract ARGB components for linear interpolation // very cool bit manipulation ah?
            val aS = (colorStart shr 24) and 0xFF
            val rS = (colorStart shr 16) and 0xFF
            val gS = (colorStart shr 8) and 0xFF
            val bS = colorStart and 0xFF

            val aE = (colorEnd shr 24) and 0xFF
            val rE = (colorEnd shr 16) and 0xFF
            val gE = (colorEnd shr 8) and 0xFF
            val bE = colorEnd and 0xFF

            // Draw horizontal gradient using 2-pixel chunks to balance performance and visual smoothness
            val chunkSize = 2
            for (i in 0 until fillWidth step chunkSize) {
                val currentWidth = if (i + chunkSize > fillWidth) fillWidth - i else chunkSize
                val ratio = i.toFloat() / width.toFloat() // Map ratio to total width, not just fillWidth

                // Interpolate colors
                val a = Mth.lerp(ratio, aS.toFloat(), aE.toFloat()).toInt()
                val r = Mth.lerp(ratio, rS.toFloat(), rE.toFloat()).toInt()
                val g = Mth.lerp(ratio, gS.toFloat(), gE.toFloat()).toInt()
                val b = Mth.lerp(ratio, bS.toFloat(), bE.toFloat()).toInt()

                val chunkColor = (a shl 24) or (r shl 16) or (g shl 8) or b
                guiGraphics.fill(x + i, y, x + i + currentWidth, y + height, chunkColor)
            }

            // Draw the "Spark" (a bright tip at the end of the current progress)
            if (fillWidth < width) {
                val sparkColor = 0xFFFFFFFF.toInt() // Pure white
                guiGraphics.fill(x + fillWidth - 1, y, x + fillWidth, y + height, sparkColor)
            }
        }
    }

    enum class StatsAlign {
        RIGHT,
        LEFT
    }
}