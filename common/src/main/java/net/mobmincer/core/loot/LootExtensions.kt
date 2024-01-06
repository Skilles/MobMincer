package net.mobmincer.core.loot

import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition
import net.mobmincer.mixin.loot.LootPoolAccessor

object LootExtensions {

    fun LootPool.isPlayerOnly(): Boolean {
        return (this as LootPoolAccessor).conditions.any { it is LootItemKilledByPlayerCondition }
    }
}