package com.github.nahnullscience.cypher_nexus.client.gui

import com.github.nahnullscience.cypher_nexus.client.gui.components.RenderConstants.CATEGORY_TITLE_PADDING
import com.github.nahnullscience.cypher_nexus.client.gui.components.RenderConstants.ICON_SIZE
import com.github.nahnullscience.cypher_nexus.client.gui.components.RenderConstants.ICON_SIZE_HALF
import com.github.nahnullscience.cypher_nexus.client.gui.components.RenderConstants.ITEM_SIZE
import com.github.nahnullscience.cypher_nexus.client.gui.components.RenderConstants.MARGIN
import com.github.nahnullscience.cypher_nexus.client.gui.components.RenderConstants.PADDING
import com.github.nahnullscience.cypher_nexus.client.gui.components.RenderConstants.SCROLLBAR_WIDTH
import com.github.nahnullscience.cypher_nexus.client.gui.components.RenderConstants.WAND_BLOCK_MARGIN
import com.github.nahnullscience.cypher_nexus.client.gui.components.RenderConstants.WHITE
import com.github.nahnullscience.cypher_nexus.client.gui.components.RenderConstants.renderCypherIcon
import com.github.nahnullscience.cypher_nexus.init.ModDataComponents
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.AbstractCypher
import com.github.nahnullscience.cypher_nexus.mechanic.cypher.category.CypherCategory
import com.github.nahnullscience.cypher_nexus.mechanic.event.CNCommonEvents
import com.github.nahnullscience.cypher_nexus.mechanic.wand.data.WandDataInvariable
import com.github.nahnullscience.cypher_nexus.network.server.ServerboundEditWandCyphers
import com.github.nahnullscience.cypher_nexus.utility.mod.ArrayOfCyphers
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.sounds.Music
import net.minecraft.util.Mth
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.client.network.ClientPacketDistributor
import java.util.*
import kotlin.math.ceil
import kotlin.math.max

class CypherIndexScreen0(
    val cypherMap: Map<CypherCategory, List<AbstractCypher>> = mapOf()
): Screen(Component.empty()) {
    companion object {
        fun ArrayOfCyphers.fillEmpty(cypher: AbstractCypher) {
            firstEmptyIndex().let {
                if (it >= 0) {
                    this[it] = HoverContext.hoverCypher
                }
            }
        }
    }
    val indexWidth: Int get() = (width * 0.5).toInt()

    private val indexColumns: Int get() = max(1, (indexWidth - MARGIN * 2) / ITEM_SIZE)

    private val layoutBlocks = mutableListOf<CategoryBlock>()
    private var totalHeight = 100

    // ====== scrollbar ===================
    private var scrollOffset = 0.0
    private var isDraggingScrollbar = false
    private val scrollbarX: Int
        get() = indexWidth - SCROLLBAR_WIDTH - 2
    private val scrollbarHeight: Int
        get() = max(20, (this.height.toFloat() / totalHeight.toFloat() * this.height).toInt())
    private val maxScroll: Double // the maximum amount the screen can scroll down
        get() = max(0.0, totalHeight.toDouble() - height)

    // ======== wand design ===============
//    private var draggedCypher: AbstractCypher? = null
    private var wandList = listOf<ItemStack>()
    private var wandListIndex = 0
    private val editedMap = HashMap<String, List<AbstractCypher>>()
    // private var currentStack: ItemStack? = null
    private var currentEditCyphers = ArrayOfCyphers(1)
    private var currentInvariableData: WandDataInvariable? = null
    private var hasEdited = false


    init {
        // instance will be created each time player open the screen
        val localPlayer = Minecraft.getInstance().player
        if (localPlayer != null) {
            wandList = CNCommonEvents.livingGatherWandsTracking(localPlayer).wands()
            for (i in wandList.indices) {
                if (localPlayer.mainHandItem == wandList[i]) wandListIndex = i
            }
        }
        // println("get all wands in player hotbar\n$wandList")
        pickWand()
    }

    override fun init() {
        // also fire each time player resizes the window
        // maybe we should move window size-related variables here
        super.init()
        if (layoutBlocks.isEmpty()) {
            cypherMap.keys.withIndex().forEach { (i, category) ->
                layoutBlocks.add(CategoryBlock(category, cypherMap.getOrDefault(category, listOf()), i))
            }
        }
        totalHeight = layoutBlocks.sumOf { block -> block.blockHeight }
    }

    override fun extractBackground(guiGraphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTick)
        HoverContext.reset()
        guiGraphics.fill(0, 0, this.width, this.height, 0x99333333.toInt())
        guiGraphics.fill(0, 0, indexWidth, this.height, 0xCC333333.toInt()) //
        //
        // scissor test prevents rendering outside these bounds // necessary?
//        guiGraphics.enableScissor(0, 0, indexWidth, this.height)
        for (block in layoutBlocks) {
            renderCypherGrid(guiGraphics, mouseX, mouseY, block, partialTick)
        }
//        guiGraphics.disableScissor()
        renderScrollbar(guiGraphics)

        if (wandList.isNotEmpty()) {
            renderWandData(guiGraphics, mouseX, mouseY, partialTick)
        }

        // it seems methods call order decides the layer order
        if (HoverContext.isHolding) {
            HoverContext.hoverCypher?.let { cypher ->
                val drawX = mouseX - ICON_SIZE_HALF // Offset by half the icon size so the cursor holds the center of the icon
                val drawY = mouseY - ICON_SIZE_HALF
                renderCypherIcon(guiGraphics, cypher, drawX, drawY)
            }
        }
    }

//    override fun tick() {
//        super.tick()
//    }

    override fun isPauseScreen() = false
    override fun getBackgroundMusic(): Music? = super.getBackgroundMusic()

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        // Only scroll if the mouse is hovering over the left panel
        if (mouseX <= indexWidth) {
            val scrollSpeed = ITEM_SIZE.toDouble() // this will scroll one full row at a time

            // scrollY is typically 1.0 (up) or -1.0 (down)
            scrollOffset = Mth.clamp(scrollOffset - scrollY * scrollSpeed, 0.0, maxScroll)
            return true
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
    }

    override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
        // 0-left; 1-right; 2-middle
        if (event.button() == 0) {
            // Check if the user clicked horizontally within the scrollbar area
            if (maxScroll > 0 && event.x >= scrollbarX && event.x <= scrollbarX + SCROLLBAR_WIDTH) {
                isDraggingScrollbar = true
                updateScrollFromMouse(event.y)
                return true
            }

            if (HoverContext.isHoveringNonEmpty) {
                when (HoverContext.hoverType) {
                    HoverType.CYPHER_INDEX -> {}
                    HoverType.CYPHER_WAND -> {
                        // drag cyphers from wand -> rearrange
                        hasEdited = true
                        HoverContext.wandSlotOld = HoverContext.wandSlotNew
                        currentEditCyphers[HoverContext.wandSlotNew] = null // set to empty
                    }
                    else -> {}
                }
                HoverContext.isHolding = true
                return true
            }
        } else if (event.button() == 1) {
            // right click directly drop / pick cyphers without drag
            if (HoverContext.isHoveringNonEmpty) {
                when (HoverContext.hoverType) {
                    HoverType.CYPHER_INDEX -> {
                        currentEditCyphers.fillEmpty(HoverContext.hoverCypher!!)
                        hasEdited = true
                    }
                    HoverType.CYPHER_WAND -> {
                        currentEditCyphers[HoverContext.wandSlotNew] = null // set to empty
                        hasEdited = true
                    }
                    else -> {}
                }
                return true
            }
        }
        return super.mouseClicked(event, doubleClick)
    }

    override fun mouseDragged(event: MouseButtonEvent, dx: Double, dy: Double): Boolean {
        // handle scrollbar
        if (isDraggingScrollbar) {
            updateScrollFromMouse(dy)
            return true
        }
        return super.mouseDragged(event, dx, dy)
    }

    override fun mouseReleased(event: MouseButtonEvent): Boolean {
        // handle scrollbar
        if (event.button() == 0) {
            if (isDraggingScrollbar) {
                isDraggingScrollbar = false
                return true
            }
            if (HoverContext.isHovering) {
                HoverContext.isHolding = false
                when (HoverContext.hoverType) {
                    HoverType.CYPHER_INDEX -> {}
                    HoverType.CYPHER_WAND -> {
                        hasEdited = true
                        if (HoverContext.wandSlotOld >= 0) { // dragged from wand
                            currentEditCyphers.switch(HoverContext.wandSlotOld, HoverContext.wandSlotNew)
                        }
                        currentEditCyphers[HoverContext.wandSlotNew] = HoverContext.hoverCypher!!
                        println("drop cypher -> wand \n$currentEditCyphers")
                    }
                    else -> {}
                }
                return true
            }
        }
        return super.mouseReleased(event)
    }


    // ===========================================================================================================
    // ===========================================================================================================
    private fun renderCypherGrid(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, block: CategoryBlock, partialTick: Float) {
        if (!block.show) return
        val reY = block.reY - scrollOffset.toInt()

        // if (reY > this.height) return // out of border

        // render category title
        graphics.text(font, block.title, MARGIN, reY - 12, WHITE)
        ////////////////////////

        val cols = indexColumns
        for ((index, cypher) in block.list.withIndex()) {
            val col = index % cols
            val row = index / cols

            val x = MARGIN + col * ITEM_SIZE
            val y = reY + PADDING + (row * ITEM_SIZE)

            // Optimization: Only render if the icon is actually visible on screen
            if (y + ICON_SIZE > 0 && y < this.height) {
                renderCypherIcon(graphics, cypher, x, y)

                // Draw the actual icon, should fit ICON_SIZE
                // Draw a background slot for the Cypher
                // Render tooltip if hovered
                val isHovered = mouseX in x..(x + ICON_SIZE) && mouseY in y..(y + ICON_SIZE)
                if (isHovered) {
                    renderHoverOverlay(graphics, cypher, x, y)
                    renderCypherTooltip(graphics, cypher, mouseX, mouseY)
                    HoverContext.hoverType = HoverType.CYPHER_INDEX
                    HoverContext.hoverCypher = cypher
                }
            }
        }
    }

    private fun renderCypherTooltip(graphics: GuiGraphicsExtractor, cypher: AbstractCypher, mouseX: Int, mouseY: Int) {
        if (HoverContext.isHolding) return
        if (cypher.isEmpty()) return
        // FIXME too ugly
//        val components = mutableListOf<ClientTooltipComponent>()
//
//        val titleText = cypher.translation().withStyle(ChatFormatting.GOLD)
//        components.add(ClientTooltipComponent.create(titleText.visualOrderText))
//        val descriptionText = cypher.description().withStyle(ChatFormatting.GRAY)
////        components.add(CypherDescriptionTooltip(CypherDescriptionTooltip.TooltipDataBundle(descText, cypher.texture())))
//
//        for (c in cypher.attributesTooltip) {
//            components.add(ClientTooltipComponent.create(c.visualOrderText))
//        }
//
//        graphics.tooltip(font, components, mouseX, mouseY, DefaultTooltipPositioner.INSTANCE, null)

        val componentsList = mutableListOf<Component>()
        val titleText = cypher.translation().withStyle(ChatFormatting.GOLD)
        componentsList.add(titleText)
        componentsList.addAll(cypher.attributesTooltip)
        graphics.setTooltipForNextFrame(font, componentsList, Optional.empty(), mouseX, mouseY)
    }

    private fun renderScrollbar(graphics: GuiGraphicsExtractor) {
        if (maxScroll <= 0) return
        val scrollY = (scrollOffset / maxScroll * (this.height - scrollbarHeight)).toInt()
        // Draw track and thumb
        graphics.fill(scrollbarX, 0, scrollbarX + SCROLLBAR_WIDTH, this.height, 0xFF111111.toInt())
        graphics.fill(scrollbarX, scrollY, scrollbarX + SCROLLBAR_WIDTH, scrollY + scrollbarHeight, 0xFFAAAAAA.toInt())
    }

    // Custom helper method to calculate the new offset
    private fun updateScrollFromMouse(mouseY: Double) {
        // Offset by half the thumb height so the mouse grabs the center of the bar
        val halfThumb = scrollbarHeight / 2.0
        val trackHeight = this.height - scrollbarHeight
        // Calculate percentage (0.0 to 1.0) and clamp it
        val scrollPercentage = Mth.clamp((mouseY - halfThumb) / trackHeight, 0.0, 1.0)
        scrollOffset = scrollPercentage * maxScroll
    }


    // ===========================================================================================================
    // ===========================================================================================================
    private fun renderWandData(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTick: Float) {
        val anchorX = indexWidth + WAND_BLOCK_MARGIN
        val anchorY1 = WAND_BLOCK_MARGIN
        val anchorY2 = anchorY1 + 60
        val cols = max(1, (indexWidth - 2 * WAND_BLOCK_MARGIN) / ITEM_SIZE)

        graphics.fill(anchorX, anchorY1, width - WAND_BLOCK_MARGIN, height - WAND_BLOCK_MARGIN, 0xCC333333.toInt())

        val currentStack = wandList[wandListIndex]
        graphics.item(currentStack, anchorX, anchorY1)

        if (currentInvariableData != null) {
            val (manaMax, manaRegen) = currentInvariableData!!.chunkF
            val (draw, castDelay, rechargeTime) = currentInvariableData!!.chunkI

//            graphics.text(font, currentStack.hoverName, anchorX + 24, anchorY1, WHITE)
//            graphics.text(font, "manaMax: $manaMax", anchorX + 24, anchorY1 + 20, WHITE)
//            graphics.text(font, "manaRegen: $manaRegen", anchorX + 56, anchorY1 + 20, WHITE)

            for (i in 0 until currentEditCyphers.capacity) {
                val col = i % cols
                val row = i / cols
                val x = anchorX + PADDING + col * ITEM_SIZE
                val y = anchorY2 + PADDING + (row * ITEM_SIZE)
                val cypher = currentEditCyphers[i]

                renderWandBlocks(graphics, cypher, x, y)
                renderCypherIcon(graphics, cypher, x, y)
                // give a few tolerance
                val isHovered = mouseX in (x - 1)..(x + ICON_SIZE + 1)
                        && mouseY in (y - 1)..(y + ICON_SIZE + 1)
                if (isHovered) {
                    renderHoverOverlay(graphics, cypher, x, y)
                    renderCypherTooltip(graphics, cypher, mouseX, mouseY)
                    HoverContext.hoverType = HoverType.CYPHER_WAND
                    HoverContext.hoverCypher = cypher
                    HoverContext.wandSlotNew = i
                }
            }
        }
    }

    private fun renderWandBlocks(graphics: GuiGraphicsExtractor, cypher: AbstractCypher, x: Int, y: Int) {
        graphics.fill(x, y, x + ICON_SIZE, y + ICON_SIZE, 0xFF444444.toInt()) // bg
    }

    private fun renderHoverOverlay(graphics: GuiGraphicsExtractor, cypher: AbstractCypher, x: Int, y: Int) {
        graphics.fill(x, y, x + ICON_SIZE, y + ICON_SIZE, 0x33FFFFFF)
    }

    private fun pickWand() {
        if (hasEdited && currentInvariableData != null) {
            val u = currentInvariableData!!.uuid
            editedMap[u] = currentEditCyphers.toList()
        }
        if (wandList.isNotEmpty()) {
            hasEdited = false
            println("pickwand: $wandList\n$wandListIndex") // TODO
            val currentStack = wandList[wandListIndex]
            currentInvariableData = currentStack.get(ModDataComponents.WAND_INVARIABLE)
            val highPayload = currentStack.get(ModDataComponents.WAND_HIGH_PAYLOAD)
            if (highPayload != null) {
                currentEditCyphers = highPayload.aoc.copy()
            }
        }
        println("pickwand: $currentEditCyphers") // TODO
    }



    // ===========================================================================================================
    // ===========================================================================================================
    override fun onClose() {
        super.onClose()
        if (hasEdited && currentInvariableData != null) {
            val u = currentInvariableData!!.uuid
            ClientPacketDistributor.sendToServer(ServerboundEditWandCyphers(u, currentEditCyphers.toList()))
        }
        editedMap.forEach { (uu, cyphers) ->
            ClientPacketDistributor.sendToServer(ServerboundEditWandCyphers(uu, cyphers))
        }
    }


    private inner class CategoryBlock(
        val category: CypherCategory,
        val list: List<AbstractCypher>,
        val index: Int
    ) {
        val title: MutableComponent = category.translation()
        val show: Boolean = list.isNotEmpty()

        val blockRows: Int get() = ceil(list.size.toDouble() / indexColumns).toInt()
        val blockHeight: Int get() = blockRows * ITEM_SIZE + CATEGORY_TITLE_PADDING
        val reY: Int get() = layoutBlocks.filter { it.index < index }.sumOf { it.blockHeight } + CATEGORY_TITLE_PADDING
    }

    private enum class HoverType() {
        CYPHER_INDEX,
        CYPHER_WAND,
        WAND_LIST,
        NONE
    }

    private object HoverContext {
        var hoverType = HoverType.NONE
        private var _hoverCypher: AbstractCypher? = null
        var hoverCypher
            get() = _hoverCypher
            set(value) {
                // if (value is EmptyCypher) return
                if (isHolding) return
                _hoverCypher = value
            }
        val isHoveringNonEmpty get() = hoverCypher != null && hoverCypher!!.isNotEmpty()
        val isHovering get() = hoverCypher != null
        var isHolding = false
        var wandSlotNew = 0
        private var _wandSlotOld = -1
        var wandSlotOld
            get() = _wandSlotOld
            set(value) {
                if (isHolding) return
                _wandSlotOld = value
            }

        fun reset() {
            hoverType = HoverType.NONE
            hoverCypher = null
            wandSlotNew = 0
            wandSlotOld = -1
        }
    }
}