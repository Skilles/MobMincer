package net.mobmincer.compat.rei

import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.gui.Renderer
import me.shedaniel.rei.api.client.gui.widgets.Widget
import me.shedaniel.rei.api.client.gui.widgets.Widgets
import me.shedaniel.rei.api.client.registry.display.DisplayCategory
import me.shedaniel.rei.api.common.category.CategoryIdentifier
import me.shedaniel.rei.api.common.util.EntryStacks
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.Enchantments
import net.mobmincer.compat.jeirei.MobMincerCategory
import net.mobmincer.core.config.MobMincerConfig
import net.mobmincer.core.registry.MincerItems

class MobMincerDisplayCategory : DisplayCategory<MobMincerDisplay> {

    override fun getCategoryIdentifier(): CategoryIdentifier<out MobMincerDisplay> = MOB_MINCER_CATEGORY

    override fun getTitle(): Component = MobMincerCategory.TITLE

    override fun getIcon(): Renderer = EntryStacks.of(MincerItems.MOB_MINCER.get())

    override fun getDisplayHeight(): Int = MobMincerCategory.HEIGHT + 2

    override fun getDisplayWidth(display: MobMincerDisplay): Int = MobMincerCategory.WIDTH + 12

    override fun setupDisplay(display: MobMincerDisplay, bounds: Rectangle): MutableList<Widget> {
        val recipe = display.lootEntry
        val widgets = mutableListOf<Widget>(Widgets.createRecipeBase(bounds))

        // Constants for slot dimensions and spacing
        val slotSize = 18
        val slotSpacing = 18
        val rowSpacing = 0

        // Calculating rows and their spacing
        val dropRows = mutableListOf<List<ItemStack>>()
        var playerDropsIndex = -1
        if (recipe.nonPlayerDrops.isNotEmpty()) {
            dropRows.add(recipe.nonPlayerDrops)
        }
        if (recipe.playerDrops.isNotEmpty()) {
            dropRows.add(recipe.playerDrops)
            playerDropsIndex = dropRows.size - 1
        }

        val progressArrowWidth = 24

        val rowCount = dropRows.size
        val rowHeight = slotSize
        val totalRowHeight = rowHeight * rowCount + rowSpacing * (rowCount - 1) + slotSize
        var currentY = (MobMincerCategory.HEIGHT - totalRowHeight) / 2 + slotSize + rowSpacing + bounds.y
        val rowWidth = slotSize * 2 + progressArrowWidth
        val startX = bounds.centerX - rowWidth / 2

        widgets.add(
            Widgets.createLabel(Point(bounds.centerX, bounds.y + 7), recipe.entityType.description)
                .noShadow()
                .color(0xFF404040.toInt(), 0xFFBBBBBB.toInt())
                .centered()
        )

        for ((i, row) in dropRows.withIndex()) {
            val mincerStack = ItemStack(MincerItems.MOB_MINCER)
            val isPlayerDropsRow = i == playerDropsIndex
            if (isPlayerDropsRow) {
                mincerStack.enchant(Enchantments.SILK_TOUCH, 1)
            }

            // Calculating X positions for slots in the row
            var currentX = startX

            // Add mincer slot
            widgets.add(
                Widgets.createSlot(Rectangle(currentX, currentY, slotSize, slotSize))
                    .entry(
                        EntryStacks.of(mincerStack)
                            .tooltip(
                                recipe.entityType.description.copy().also {
                                    if (isPlayerDropsRow) {
                                        it.append(
                                            Component.translatable("mobmincer.jei.tooltip.playerKilled")
                                        )
                                    }
                                }
                            )
                    )
                    .disableBackground()
                    .markInput()
                    .tooltipsEnabled(true)
            )
            widgets.add(
                Widgets.createArrow(Point(currentX + slotSize, currentY))
                    .animationDurationTicks(MobMincerConfig.CONFIG.maxMinceTick.get().toDouble())
            )

            // Move to next slot position
            currentX += slotSpacing + progressArrowWidth

            widgets.add(
                Widgets.createSlot(Rectangle(currentX, currentY, slotSize, slotSize))
                    .entries(row.map { EntryStacks.of(it) })
                    .disableBackground()
                    .markOutput()
                    .tooltipsEnabled(true)
            )

            // Move to the next row
            currentY += slotSize + rowSpacing
        }

        return widgets
    }

    companion object {
        val MOB_MINCER_CATEGORY: CategoryIdentifier<MobMincerDisplay> = CategoryIdentifier.of(MobMincerCategory.ID)
    }
}
