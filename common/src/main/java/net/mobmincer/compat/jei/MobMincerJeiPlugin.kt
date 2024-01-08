package net.mobmincer.compat.jei

import mezz.jei.api.IModPlugin
import mezz.jei.api.JeiPlugin
import mezz.jei.api.recipe.RecipeType
import mezz.jei.api.registration.IRecipeCatalystRegistration
import mezz.jei.api.registration.IRecipeCategoryRegistration
import mezz.jei.api.registration.IRecipeRegistration
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.Enchantments
import net.mobmincer.MobMincer
import net.mobmincer.compat.jeirei.MobMincerCategory
import net.mobmincer.core.loot.KillDropLootEntry
import net.mobmincer.core.registry.AttachmentRegistry
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


        registration.addRecipes(LOOT_TYPE, KillDropLootEntry.createAll())
    }

    override fun registerRecipeCatalysts(registration: IRecipeCatalystRegistration) {
        val mincerStack = ItemStack(MincerItems.MOB_MINCER)
        registration.addRecipeCatalyst(mincerStack, LOOT_TYPE)
        val enchantedStack = mincerStack.copy()
        enchantedStack.enchant(Enchantments.SILK_TOUCH, 1)
        registration.addRecipeCatalyst(enchantedStack, LOOT_TYPE)
    }

    companion object {
        val LOOT_TYPE: RecipeType<KillDropLootEntry> = RecipeType(MobMincerCategory.ID, KillDropLootEntry::class.java)
    }
}
