package net.mobmincer.compat.jei

import mezz.jei.api.IModPlugin
import mezz.jei.api.JeiPlugin
import mezz.jei.api.registration.*
import mezz.jei.api.runtime.IJeiRuntime
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.item.ItemStack
import net.mobmincer.MobMincer
import net.mobmincer.core.registry.MincerItems

@JeiPlugin
class MobMincerJeiPlugin : IModPlugin {
    override fun getPluginUid(): ResourceLocation {
        return ResourceLocation(MobMincer.MOD_ID, "jei_plugin")
    }

    override fun registerCategories(registration: IRecipeCategoryRegistration) {
        registration.addRecipeCategories(MobMincerRecipeCategory(registration.jeiHelpers))
    }

    override fun registerRuntime(registration: IRuntimeRegistration) {

    }

    override fun registerRecipes(registration: IRecipeRegistration) {
        registration.addItemStackInfo(ItemStack(MincerItems.MOB_MINCER), Component.literal("testing"))
        val recipes = mutableListOf<MobMincerRecipe>()
        BuiltInRegistries.ENTITY_TYPE.forEach {
            if (it.category == MobCategory.CREATURE || it.category == MobCategory.MONSTER) {
                recipes.add(MobMincerRecipe(it))
            }
        }
        registration.addRecipes(MobMincerRecipe.TYPE, recipes)
    }

    override fun registerRecipeCatalysts(registration: IRecipeCatalystRegistration) {
        registration.addRecipeCatalyst(ItemStack(MincerItems.MOB_MINCER), MobMincerRecipe.TYPE)
    }

    override fun registerGuiHandlers(registration: IGuiHandlerRegistration) {

    }

    override fun registerAdvanced(registration: IAdvancedRegistration) {

    }

    override fun onRuntimeAvailable(jeiRuntime: IJeiRuntime) {

    }
}