package net.mobmincer.core.loot

import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.storage.loot.LootParams
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import net.minecraft.world.level.storage.loot.parameters.LootContextParams

object LootFactoryCache {
    private val cache = mutableMapOf<Class<out Mob>, LootFactory>()
    private val killedByPlayerCache = mutableMapOf<Class<out Mob>, LootFactory>()

    fun getLootFactory(mob: Mob, killedByPlayer: Boolean = false, lootingLevel: Int = 0): LootFactory {
        if (mob.level().isClientSide) {
            return LootFactory.EMPTY
        }

        val key = mob.javaClass
        if (lootingLevel != 0) {
            return mob.createLootFactory(null, killedByPlayer, lootingLevel)
        }
        if (killedByPlayer) {
            return killedByPlayerCache.getOrPut(key) { mob.createLootFactory(null, true, 0) }
        }
        return cache.getOrPut(key) { mob.createLootFactory(null, false, 0) }
    }

    fun hasLoot(mob: Mob, killedByPlayer: Boolean = false): Boolean {
        val factory = getLootFactory(mob, killedByPlayer)
        return factory != LootFactory.EMPTY
    }

    private fun Mob.createLootFactory(damager: Entity?, killedByPlayer: Boolean, lootingLevel: Int): LootFactory {
        val resourceLocation: ResourceLocation = this.lootTable
        val level = this.level() as ServerLevel
        val lootTable = level.server.lootData.getLootTable(resourceLocation)
        val builder = LootParams.Builder(level)
            .withParameter(LootContextParams.THIS_ENTITY, this)
            .withParameter(LootContextParams.ORIGIN, this.position())
            .withParameter(
                LootContextParams.DAMAGE_SOURCE,
                damager?.let { level.damageSources().thorns(it) } ?: level.damageSources().generic())
            .withOptionalParameter(LootContextParams.KILLER_ENTITY, damager)
            .withOptionalParameter(LootContextParams.DIRECT_KILLER_ENTITY, damager)

        if (killedByPlayer) {
            builder.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, damager as? Player)
        }

        val lootParams = builder.create(LootContextParamSets.ENTITY)

        val hasLoot = lootTable.getRandomItems(lootParams, this.lootTableSeed).isNotEmpty()

        if (!hasLoot) {
            return LootFactory.EMPTY
        }

        return LootFactory { lootTable.getRandomItems(lootParams, this.lootTableSeed) }
    }
}

