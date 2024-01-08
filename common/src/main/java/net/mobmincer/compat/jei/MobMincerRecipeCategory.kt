package net.mobmincer.compat.jei

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder
import mezz.jei.api.gui.drawable.IDrawable
import mezz.jei.api.gui.drawable.IDrawableAnimated
import mezz.jei.api.gui.ingredient.IRecipeSlotsView
import mezz.jei.api.helpers.IJeiHelpers
import mezz.jei.api.recipe.IFocusGroup
import mezz.jei.api.recipe.RecipeIngredientRole
import mezz.jei.api.recipe.RecipeType
import mezz.jei.api.recipe.category.IRecipeCategory
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.Enchantments
import net.mobmincer.compat.jeirei.MobMincerCategory
import net.mobmincer.core.loot.KillDropLootEntry
import net.mobmincer.core.loot.LootLookup
import net.mobmincer.core.registry.MincerItems

class MobMincerRecipeCategory(private val helpers: IJeiHelpers) : IRecipeCategory<KillDropLootEntry> {

    private val progressArrow: IDrawable = helpers.guiHelper.drawableBuilder(PROGRESS_SPRITE, 0, 0, 24, 16)
        .setTextureSize(24, 17)
        .buildAnimated(100, IDrawableAnimated.StartDirection.LEFT, false)

    companion object {
        const val HEIGHT = 60
        const val WIDTH = 75

        private val PROGRESS_SPRITE = ResourceLocation(
            "textures/gui/sprites/container/furnace/burn_progress.png"
        )
    }

    override fun getRecipeType(): RecipeType<KillDropLootEntry> = MobMincerJeiPlugin.LOOT_TYPE

    override fun getTitle(): Component = MobMincerCategory.TITLE

    override fun getBackground(): IDrawable = helpers.guiHelper.createBlankDrawable(WIDTH, HEIGHT)

    override fun getIcon(): IDrawable = helpers.guiHelper.createDrawableItemStack(ItemStack(MincerItems.MOB_MINCER))

    override fun isHandled(recipe: KillDropLootEntry): Boolean = LootLookup.hasLoot(recipe.lootTable)

    override fun setRecipe(builder: IRecipeLayoutBuilder, recipe: KillDropLootEntry, focuses: IFocusGroup) {
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
        val rowCount = dropRows.size
        val rowHeight = slotSize
        val totalRowHeight = rowHeight * rowCount + rowSpacing * (rowCount - 1) + slotSize
        var currentY = (HEIGHT - totalRowHeight) / 2 + slotSize + rowSpacing + 3
        val rowWidth = slotSize * 2 + progressArrow.width
        val startX = (WIDTH - rowWidth) / 2

        recipe.spawnEgg?.let {
            builder.addInvisibleIngredients(RecipeIngredientRole.INPUT)
                .addItemStack(it)
        }

        for ((i, row) in dropRows.withIndex()) {
            val mincerStack = ItemStack(MincerItems.MOB_MINCER)
            val isPlayerDropsRow = i == playerDropsIndex
            if (isPlayerDropsRow) {
                mincerStack.enchant(Enchantments.SILK_TOUCH, 1)
            }

            // Calculating X positions for slots in the row
            var currentX = startX

            // Add mincer slot
            builder.addSlot(RecipeIngredientRole.CATALYST, currentX, currentY)
                .setSlotName("mincer${if (isPlayerDropsRow) "_player" else ""}")
                .addItemStack(mincerStack)
                .setOverlay(progressArrow, slotSize, 0)
                .addTooltipCallback { _, tooltip ->
                    tooltip.add(recipe.entityType.description)
                }

            // Move to next slot position
            currentX += slotSpacing + progressArrow.width

            // Add drop slot
            builder.addSlot(RecipeIngredientRole.OUTPUT, currentX, currentY)
                .setSlotName("drop")
                .addItemStacks(row)

            // Move to the next row
            currentY += slotSize + rowSpacing
        }
    }

    override fun draw(recipe: KillDropLootEntry, recipeSlotsView: IRecipeSlotsView, guiGraphics: GuiGraphics, mouseX: Double, mouseY: Double) {
        val minecraft = Minecraft.getInstance()

        val centerTextX = (WIDTH - minecraft.font.width(recipe.entityType.description)) / 2
        val centerItemX = (WIDTH - 16) / 2
        guiGraphics.drawString(
            minecraft.font,
            recipe.entityType.description,
            centerTextX,
            0,
            0x404040,
            false
        )
        recipe.spawnEgg?.let {
            guiGraphics.renderFakeItem(
                it,
                centerItemX,
                7
            )
        }
    }
}
