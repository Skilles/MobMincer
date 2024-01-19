package net.mobmincer.core.recipe

import net.minecraft.core.NonNullList
import net.minecraft.world.inventory.CraftingContainer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.CraftingBookCategory
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.ShapedRecipe
import net.minecraft.world.level.Level
import net.mobmincer.core.item.MobMincerType
import net.mobmincer.core.item.MobMincerType.Companion.getMincerType
import net.mobmincer.core.item.MobMincerType.Companion.setMincerType
import net.mobmincer.core.registry.MMContent

class MincerUpgradeRecipe(category: CraftingBookCategory) : ShapedRecipe(
    "",
    category,
    3,
    3,
    NonNullList.of(
        Ingredient.EMPTY,
        Ingredient.of(Items.BLAZE_POWDER),
        Ingredient.of(Items.REDSTONE_BLOCK),
        Ingredient.of(Items.BLAZE_POWDER),
        Ingredient.of(Items.REDSTONE_BLOCK),
        Ingredient.of(MMContent.MOB_MINCER_ITEM.get()),
        Ingredient.of(Items.REDSTONE_BLOCK),
        Ingredient.of(Items.BLAZE_POWDER),
        Ingredient.of(Items.REDSTONE_BLOCK),
        Ingredient.of(Items.BLAZE_POWDER)
    ),
    ItemStack(MMContent.MOB_MINCER_ITEM).also { it.setMincerType(MobMincerType.POWERED) }
) {

    override fun matches(container: CraftingContainer, level: Level): Boolean {
        if (!super.matches(container, level)) {
            return false
        }

        return container.getItem(4).getMincerType() == MobMincerType.BASIC
    }

    override fun getSerializer(): RecipeSerializer<*> {
        return RecipeSerializers.MINCER_POWER_UPGRADE
    }
}
