package net.mobmincer.core.loot

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob

@Deprecated("Use LootLookup instead")
object LootFactoryCache {
    private val noLootCache = mutableMapOf<Pair<Class<out Mob>, Boolean>, Boolean>()

    fun getLootFactory(mob: Mob, killedByPlayer: Boolean = false, lootingLevel: Int = 0): LootFactory {
        if (mob.level().isClientSide) {
            return LootFactory.EMPTY
        }

        return mob.createLootFactory(killedByPlayer, lootingLevel)
    }

    fun hasLoot(mob: Mob, killedByPlayer: Boolean = false): Boolean {
        return !LootLookup.hasLoot(mob, killedByPlayer)
    }

    fun reset() {
        noLootCache.clear()
    }

    private fun LivingEntity.createLootFactory(killedByPlayer: Boolean, lootingLevel: Int): LootFactory {
        return LootFactory.create(this, killedByPlayer, lootingLevel)
    }
}
