package net.mobmincer.core.loot

import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.SpawnEggItem

data class KillDropLootEntry(private val entityType: EntityType<*>) {
    val spawnEgg: ItemStack?
        get() = SpawnEggItem.byId(entityType)?.let { ItemStack(it) }

    private val outputs: LootStack = LootStack.from(entityType.defaultLootTable)

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
}
