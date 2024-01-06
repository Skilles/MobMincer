package net.mobmincer.core.loot

import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.entries.LootItem
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer
import net.mobmincer.core.loot.LootExtensions.isPlayerOnly
import net.mobmincer.mixin.loot.LootItemAccessor
import net.mobmincer.mixin.loot.LootPoolAccessor
import net.mobmincer.mixin.loot.LootTableAccessor

interface LootStack : List<LootStack.LootStackEntry> {

    val playerOnly: List<ItemStack>

    val nonPlayerOnly: List<ItemStack>

    fun generateServerLoot(entity: LivingEntity, includePlayerLoot: Boolean, lootingLevel: Int): ObjectArrayList<ItemStack>

    companion object {
        val EMPTY: LootStack = LootStackImpl.EMPTY

        fun from(lootId: ResourceLocation): LootStack {
            return LootLookup.get(lootId) ?: error("Loot table $lootId not found")
        }

        fun from(loot: LootTable): LootStack {
            val items = getPossibleLoot(loot)
            if (items.isEmpty()) {
                return EMPTY
            }
            return LootStackImpl(loot, items)
        }

        private fun getPossibleLoot(table: LootTable): List<LootStackEntry> {
            val lootPools = (table as LootTableAccessor).pools
            val stackEntries = mutableListOf<LootStackEntry>()
            for (pool in lootPools) {
                val entries: List<LootPoolEntryContainer> = (pool as LootPoolAccessor).entries
                for (entry in entries) {
                    if (entry is LootItem) {
                        stackEntries.add(createEntry(entry, pool))
                    }
                }
            }
            return stackEntries
        }

        private fun createEntry(
            item: LootItem,
            pool: LootPool
        ): LootStackEntry = LootStackEntry((item as LootItemAccessor).item.value(), pool.isPlayerOnly())
    }

    data class LootStackEntry(val item: Item, val playerOnly: Boolean)
}
