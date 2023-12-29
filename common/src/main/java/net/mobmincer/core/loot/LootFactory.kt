package net.mobmincer.core.loot

import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.minecraft.world.item.ItemStack

fun interface LootFactory {
    fun generateLoot(): ObjectArrayList<ItemStack>

    companion object {
        val EMPTY = LootFactory { ObjectArrayList() }
    }
}
