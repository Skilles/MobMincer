package net.mobmincer.core.loot

import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.entries.LootItem
import net.minecraft.world.level.storage.loot.functions.LootItemFunction
import net.minecraft.world.level.storage.loot.functions.SetPotionFunction
import net.minecraft.world.level.storage.loot.predicates.DamageSourceCondition
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition
import net.mobmincer.mixin.loot.LootItemAccessor
import net.mobmincer.mixin.loot.LootPoolAccessor
import net.mobmincer.mixin.loot.LootPoolEntryContainerAccessor
import net.mobmincer.mixin.loot.LootPoolSingletonContainerAccessor

object LootExtensions {

    fun LootPool.isPlayerOnly(): Boolean {
        return (this as LootPoolAccessor).conditions.any { it is LootItemKilledByPlayerCondition }
    }

    fun LootItem.getStack(): ItemStack? {
        val conditions = (this as LootPoolEntryContainerAccessor).conditions
        if (conditions.any { it is DamageSourceCondition }) {
            return null
        }
        val item = (this as LootItemAccessor).item.value()
        val functions = (this as LootPoolSingletonContainerAccessor).functions
        val stack = ItemStack(item)
        functions.forEach { it.safelyApply(stack) }
        return stack
    }

    /**
     * Skips applying functions that require a loot context if one is not provided.
     */
    private fun LootItemFunction.safelyApply(stack: ItemStack, lootContext: LootContext? = null) {
        if (this is SetPotionFunction) {
            this.apply(stack, lootContext)
        } else if (lootContext != null) {
            this.apply(stack, lootContext)
        }
    }
}
