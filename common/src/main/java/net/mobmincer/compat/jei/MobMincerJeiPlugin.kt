package net.mobmincer.compat.jei

import mezz.jei.api.IModPlugin
import mezz.jei.api.JeiPlugin
import mezz.jei.api.registration.IRecipeCatalystRegistration
import mezz.jei.api.registration.IRecipeCategoryRegistration
import mezz.jei.api.registration.IRecipeRegistration
import net.minecraft.ChatFormatting
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.Enchantments
import net.mobmincer.MobMincer
import net.mobmincer.core.attachment.AttachmentRegistry
import net.mobmincer.core.registry.MincerItems

@JeiPlugin
class MobMincerJeiPlugin : IModPlugin {
    override fun getPluginUid(): ResourceLocation {
        return ResourceLocation(MobMincer.MOD_ID, "jei_plugin")
    }

    override fun registerCategories(registration: IRecipeCategoryRegistration) {
        registration.addRecipeCategories(MobMincerRecipeCategory(registration.jeiHelpers))
    }

    override fun registerRecipes(registration: IRecipeRegistration) {
        val recipes = mutableListOf<MobMincerRecipe>()
        val infoEntries = AttachmentRegistry.getEntries().map { (key, value) ->
            value.name.copy().withStyle(ChatFormatting.DARK_AQUA).append(
                key.item.description.copy().append(
                " - "
                )
            ) as Component
        }.toTypedArray()
        registration.addItemStackInfo(
            ItemStack(MincerItems.MOB_MINCER),
            Component.translatable("mobmincer.jei.info.header"),
            *infoEntries
        )

        BuiltInRegistries.ENTITY_TYPE.forEach {
            if (it.category != MobCategory.MISC) {
                recipes.add(MobMincerRecipe(it))
            }
        }
        registration.addRecipes(MobMincerRecipe.TYPE, recipes)
    }

    override fun registerRecipeCatalysts(registration: IRecipeCatalystRegistration) {
        val mincerStack = ItemStack(MincerItems.MOB_MINCER)
        registration.addRecipeCatalyst(mincerStack, MobMincerRecipe.TYPE)
        val enchantedStack = mincerStack.copy()
        enchantedStack.enchant(Enchantments.SILK_TOUCH, 1)
        registration.addRecipeCatalyst(enchantedStack, MobMincerRecipe.TYPE)
    }
}
