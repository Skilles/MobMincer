package net.mobmincer.core.loot

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.Item
import net.minecraft.world.item.SpawnEggItem

object SpawnEggLookup {
    private val lookup: Map<EntityType<*>, SpawnEggItem> = mutableMapOf()

    fun get(entityType: EntityType<*>): Item? {
        return lookup[entityType]
    }

    fun get(entityType: EntityType<*>, default: Item): Item {
        return lookup[entityType] ?: default
    }

    private fun register(entityType: EntityType<*>, spawnEgg: SpawnEggItem) {
        (lookup as MutableMap)[entityType] = spawnEgg
    }

    fun setup() {
        BuiltInRegistries.ITEM.forEach {
            if (it is SpawnEggItem) {
                val entityType = it.getType(null)
                register(entityType, it)
            }
        }
    }
}
