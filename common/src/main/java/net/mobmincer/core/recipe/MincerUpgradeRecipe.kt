package net.mobmincer.core.recipe

import net.minecraft.core.RegistryAccess
import net.minecraft.world.inventory.CraftingContainer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.CraftingBookCategory
import net.minecraft.world.item.crafting.CustomRecipe
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer
import net.minecraft.world.level.Level

class MincerUpgradeRecipe(category: CraftingBookCategory) : CustomRecipe(category) {
    companion object {
        val SERIALIZER: RecipeSerializer<MincerUpgradeRecipe> = RecipeSerializer.register(
            "mob_mincer_upgrade",
            SimpleCraftingRecipeSerializer(::MincerUpgradeRecipe)
        )
    }

    override fun matches(container: CraftingContainer, level: Level): Boolean {
        TODO("Not yet implemented")
    }

    override fun assemble(container: CraftingContainer, registryAccess: RegistryAccess): ItemStack {
        TODO("Not yet implemented")
    }

    override fun canCraftInDimensions(width: Int, height: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun getSerializer(): RecipeSerializer<*> {
        return SERIALIZER
    }
}
