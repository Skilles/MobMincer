package net.mobmincer.core.loot

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.SpawnEggItem

data class KillDropLootEntry(val entityType: EntityType<*>) {
    val spawnEgg: ItemStack? by lazy { SpawnEggItem.byId(entityType)?.let { ItemStack(it) } }

    private val outputs: LootStack = LootStack.from(lootTable)

    val playerDrops: List<ItemStack>
        get() = outputs.playerOnly

    val nonPlayerDrops: List<ItemStack>
        get() = outputs.nonPlayerOnly

    val allDrops: List<ItemStack>
        get() = playerDrops + nonPlayerDrops

    val hasPlayerDrops: Boolean
        get() = outputs.playerOnly.isNotEmpty()

    val isEmpty: Boolean
        get() = outputs.isEmpty()

    val lootTable: ResourceLocation
        get() = entityType.defaultLootTable

    companion object {
        fun createAll(): List<KillDropLootEntry> = BuiltInRegistries.ENTITY_TYPE
            .filter { LootLookup.hasLoot(it.defaultLootTable) }
            .map { KillDropLootEntry(it) }
    }
}
