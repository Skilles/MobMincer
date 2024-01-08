package net.mobmincer.core.loot

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.storage.loot.LootTable
import java.util.concurrent.ConcurrentHashMap

/**
 * Used to lookup loot tables for entities. On the server, this can be used to generate loot for entities.
 */
object LootLookup {
    private val ID_TO_LOOT: MutableMap<ResourceLocation, LootStack> =
        ConcurrentHashMap<ResourceLocation, LootStack>()

    fun hasLoot(identifier: ResourceLocation): Boolean {
        return (ID_TO_LOOT[identifier] ?: return false) != LootStack.EMPTY
    }

    fun hasLoot(entity: LivingEntity): Boolean {
        return hasLoot(entity.lootTable)
    }

    fun hasLoot(entity: LivingEntity, killedByPlayer: Boolean): Boolean {
        return hasLoot(entity.lootTable) && (killedByPlayer || ID_TO_LOOT[entity.lootTable]!!.nonPlayerOnly.isNotEmpty())
    }

    fun get(identifier: ResourceLocation): LootStack? {
        return ID_TO_LOOT[identifier]
    }

    fun get(entity: LivingEntity): LootStack? {
        return get(entity.lootTable)
    }

    fun set(identifier: ResourceLocation, loot: LootTable) {
        ID_TO_LOOT[identifier] = LootStack.from(loot)
    }

    fun clear() {
        ID_TO_LOOT.clear()
    }
}
