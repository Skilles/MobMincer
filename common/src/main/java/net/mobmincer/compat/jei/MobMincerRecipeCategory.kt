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
import net.minecraft.locale.Language
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.Enchantments
import net.mobmincer.MobMincer
import net.mobmincer.core.loot.LootLookup
import net.mobmincer.core.registry.MincerItems

class MobMincerRecipeCategory(private val helpers: IJeiHelpers) : IRecipeCategory<MobMincerRecipe> {
    companion object {
        const val HEIGHT = 60
        const val WIDTH = 75

        private val PROGRESS_SPRITE = ResourceLocation(
            "textures/gui/sprites/container/furnace/burn_progress.png"
        )
    }

    override fun getRecipeType(): RecipeType<MobMincerRecipe> {
        return RecipeType.create(MobMincer.MOD_ID, "mob_mincer", MobMincerRecipe::class.java)
    }

    override fun getTitle(): Component {
        return Component.translatable("mob_mincer.jei.category.title")
    }

    override fun getBackground(): IDrawable {
        return helpers.guiHelper.createBlankDrawable(WIDTH, HEIGHT)
    }

    override fun getIcon(): IDrawable {
        return helpers.guiHelper.createDrawableItemStack(ItemStack(MincerItems.MOB_MINCER))
    }

    override fun setRecipe(builder: IRecipeLayoutBuilder, recipe: MobMincerRecipe, focuses: IFocusGroup) {
        val outputs = recipe.lootEntry

        // Constants for slot dimensions and spacing
        val slotSize = 18
        val slotSpacing = 18
        val rowSpacing = 0

        // Calculating rows and their spacing
        val dropRows = mutableListOf<List<ItemStack>>()
        var playerDropsIndex = -1
        if (outputs.nonPlayerDrops.isNotEmpty()) {
            dropRows.add(outputs.nonPlayerDrops)
        }
        if (outputs.playerDrops.isNotEmpty()) {
            dropRows.add(outputs.playerDrops)
            playerDropsIndex = dropRows.size - 1
        }
        val rowCount = dropRows.size
        val rowHeight = slotSize
        val totalRowHeight = rowHeight * rowCount + rowSpacing * (rowCount - 1) + slotSize
        var currentY = (HEIGHT - totalRowHeight) / 2

        val progressArrow = helpers.guiHelper.drawableBuilder(PROGRESS_SPRITE, 0, 0, 24, 16)
            .setTextureSize(24, 17)
            .buildAnimated(100, IDrawableAnimated.StartDirection.LEFT, false)

        // Centering the spawn egg slot at the top
        val spawnEggX = (WIDTH - slotSize) / 2
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, spawnEggX, 6)
            .setSlotName("spawn_egg")
            .addItemStack(outputs.spawnEgg)

        // Adjust Y position for the first row
        currentY += slotSize + rowSpacing
        val rowWidth = slotSize * 2 + progressArrow.width
        val startX = (WIDTH - rowWidth) / 2

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

    override fun isHandled(recipe: MobMincerRecipe): Boolean {
        return LootLookup.hasLoot(recipe.location)
    }

    override fun draw(recipe: MobMincerRecipe, recipeSlotsView: IRecipeSlotsView, guiGraphics: GuiGraphics, mouseX: Double, mouseY: Double) {
        val minecraft = Minecraft.getInstance()

        guiGraphics.drawString(
            minecraft.font,
            Language.getInstance().getVisualOrder(recipe.entityType.description),
            (WIDTH - minecraft.font.width(recipe.entityType.description)) / 2,
            0,
            -0x1000000,
            false
        )
    }
}
