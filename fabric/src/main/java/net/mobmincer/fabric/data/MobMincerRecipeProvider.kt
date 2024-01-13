package net.mobmincer.fabric.data

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider
import net.minecraft.data.recipes.RecipeCategory
import net.minecraft.data.recipes.RecipeOutput
import net.minecraft.data.recipes.ShapedRecipeBuilder
import net.minecraft.data.recipes.SpecialRecipeBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Items
import net.mobmincer.MobMincer
import net.mobmincer.core.recipe.MincerUpgradeRecipe
import net.mobmincer.core.registry.MMContent

// We don't use this in Forge, so we don't have to worry about mappings
class MobMincerRecipeProvider(output: FabricDataOutput) : FabricRecipeProvider(output) {

    override fun buildRecipes(recipeOutput: RecipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, MMContent.MOB_MINCER_ITEM.get(), 1)
            .pattern(" I ")
            .pattern("IEI")
            .pattern("ISI")
            .define('I', Items.INK_SAC)
            .define('E', Items.ENDER_EYE)
            .define('S', Items.DIAMOND_SWORD)
            .unlockedBy("has_ink_sac", has(Items.INK_SAC))
            .save(recipeOutput, ResourceLocation(MobMincer.MOD_ID, "mob_mincer"))
        SpecialRecipeBuilder.special(
            MincerUpgradeRecipe.SERIALIZER
        ).save(recipeOutput, ResourceLocation(MobMincer.MOD_ID, "mob_mincer_upgrade"))
    }
}
