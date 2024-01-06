package net.mobmincer.compat.jei

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder
import mezz.jei.api.gui.drawable.IDrawable
import mezz.jei.api.gui.ingredient.IRecipeSlotsView
import mezz.jei.api.helpers.IJeiHelpers
import mezz.jei.api.recipe.IFocusGroup
import mezz.jei.api.recipe.RecipeIngredientRole
import mezz.jei.api.recipe.RecipeType
import mezz.jei.api.recipe.category.IRecipeCategory
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.Enchantments
import net.mobmincer.MobMincer
import net.mobmincer.core.loot.LootLookup
import net.mobmincer.core.registry.MincerItems

class MobMincerRecipeCategory(private val helpers: IJeiHelpers) : IRecipeCategory<MobMincerRecipe> {
    companion object {
        val HEIGHT = 200
        val WIDTH = 150
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

        val rows = if (outputs.hasPlayerDrops) 2 else 1
        val rowSpacing = 18
        val colSpacing = 18
        val startX = (WIDTH - 18 * 3) / 2
        var currentY = (HEIGHT - 18 * rows) / 2
        var currentX = startX

        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, width / 2, 20)
            .setSlotName("spawn_egg")
            .addItemStack(recipe.lootEntry.spawnEgg)

        var rowsLeft = rows
        while (rowsLeft > 0) {
            val isPlayerDropsRow = rows == 2 && rowsLeft == 2
            val mincerStack = ItemStack(MincerItems.MOB_MINCER)
            if (isPlayerDropsRow) {
                mincerStack.enchant(Enchantments.SILK_TOUCH, 1)
            }
            builder.addSlot(RecipeIngredientRole.RENDER_ONLY, currentX, currentY)
                .setSlotName(
                    "mincer${
                        if (isPlayerDropsRow) "_player" else ""
                    }"
                )
                .addItemStack(mincerStack)
            currentX += colSpacing
            builder.addSlot(RecipeIngredientRole.RENDER_ONLY, currentX, currentY)
                .setSlotName("drop")
                .addItemStacks(if (isPlayerDropsRow) outputs.playerDrops else outputs.nonPlayerDrops)
            currentY += rowSpacing
            currentX = startX
            rowsLeft--
        }
    }

    override fun isHandled(recipe: MobMincerRecipe): Boolean {
        return LootLookup.hasLoot(recipe.location)
    }

    override fun draw(recipe: MobMincerRecipe, recipeSlotsView: IRecipeSlotsView, guiGraphics: GuiGraphics, mouseX: Double, mouseY: Double) {
        super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY)
    }
}