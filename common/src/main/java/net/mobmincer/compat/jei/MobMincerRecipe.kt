package net.mobmincer.compat.jei

import mezz.jei.api.recipe.RecipeType
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.EntityType
import net.mobmincer.MobMincer
import net.mobmincer.core.loot.KillDropLootEntry

class MobMincerRecipe(val entityType: EntityType<*>) {
    val location: ResourceLocation
        get() = entityType.defaultLootTable

    val lootEntry = KillDropLootEntry(entityType)

    companion object {
        val TYPE: RecipeType<MobMincerRecipe> = RecipeType.create(
            MobMincer.MOD_ID,
            "mobmincer",
            MobMincerRecipe::class.java
        )
    }
}
