package net.mobmincer.core.recipe

import net.minecraft.world.item.crafting.CraftingRecipe
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer

object RecipeSerializers {

    lateinit var MINCER_POWER_UPGRADE: RecipeSerializer<out CraftingRecipe> private set


    fun register() {
        MINCER_POWER_UPGRADE = RecipeSerializer.register(
            "mob_mincer_upgrade",
            SimpleCraftingRecipeSerializer(::MincerUpgradeRecipe)
        )
    }
}