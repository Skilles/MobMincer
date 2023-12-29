package net.mobmincer.core.loot

import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.Mob
import net.minecraft.world.level.storage.loot.LootParams
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import net.minecraft.world.level.storage.loot.parameters.LootContextParams

object LootFactoryCache {
    private val cache = mutableMapOf<Class<out Mob>, LootFactory>()

    fun getLootFactory(mob: Mob): LootFactory {
        val key = mob.javaClass
        return cache.getOrPut(key) { mob.createLootFactory(null) }
    }

    fun hasLoot(mob: Mob): Boolean {
        val factory = getLootFactory(mob)
        if (factory == LootFactory.EMPTY) {
            return false
        }
        return !factory.generateLoot().isEmpty
    }

    private fun Mob.createLootFactory(damager: Entity?): LootFactory {
        val resourceLocation: ResourceLocation = this.lootTable
        val lootTable = level().server!!.lootData.getLootTable(resourceLocation)
        val builder = LootParams.Builder(level() as ServerLevel)
            .withParameter(LootContextParams.THIS_ENTITY, this)
            .withParameter(LootContextParams.ORIGIN, this.position())
            .withParameter(
                LootContextParams.DAMAGE_SOURCE,
                damager?.let { this.level().damageSources().thorns(it) } ?: this.level().damageSources().generic())
            .withOptionalParameter(LootContextParams.KILLER_ENTITY, damager)
            .withOptionalParameter(LootContextParams.DIRECT_KILLER_ENTITY, damager)
        val lootParams = builder.create(LootContextParamSets.ENTITY)

        val hasLoot = lootTable.getRandomItems(lootParams, this.lootTableSeed).isNotEmpty()

        if (!hasLoot) {
            return LootFactory.EMPTY
        }

        return LootFactory { lootTable.getRandomItems(lootParams, this.lootTableSeed) }
    }
}

