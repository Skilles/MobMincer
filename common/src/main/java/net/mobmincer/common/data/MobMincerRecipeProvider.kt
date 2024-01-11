package net.mobmincer.common.data

import dev.architectury.injectables.annotations.PlatformOnly
import net.minecraft.data.PackOutput
import net.minecraft.data.recipes.*
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Items
import net.mobmincer.MobMincer
import net.mobmincer.core.recipe.MincerUpgradeRecipe
import net.mobmincer.core.registry.MincerItems

// We don't use this in Forge, so we don't have to worry about mappings
class MobMincerRecipeProvider(output: PackOutput) : RecipeProvider(output) {
    @PlatformOnly(PlatformOnly.FABRIC)
    override fun buildRecipes(recipeOutput: RecipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, MincerItems.MOB_MINCER.get(), 1)
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
