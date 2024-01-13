package net.mobmincer.compat.rei

import me.shedaniel.rei.api.common.category.CategoryIdentifier
import me.shedaniel.rei.api.common.display.Display
import me.shedaniel.rei.api.common.entry.EntryIngredient
import me.shedaniel.rei.api.common.util.EntryIngredients
import net.minecraft.world.item.ItemStack
import net.mobmincer.core.loot.KillDropLootEntry
import net.mobmincer.core.registry.MMContent

class MobMincerDisplay(val lootEntry: KillDropLootEntry) : Display {

    override fun getInputEntries(): MutableList<EntryIngredient> {
        return mutableListOf(
            EntryIngredients.ofItemStacks(listOf(ItemStack(MMContent.MOB_MINCER_ITEM), lootEntry.spawnEgg))
        )
    }

    override fun getOutputEntries(): MutableList<EntryIngredient> {
        return mutableListOf(EntryIngredients.ofItemStacks(lootEntry.allDrops))
    }

    override fun getCategoryIdentifier(): CategoryIdentifier<*> {
        return MobMincerDisplayCategory.MOB_MINCER_CATEGORY
    }
}
