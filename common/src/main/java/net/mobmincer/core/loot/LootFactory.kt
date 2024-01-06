package net.mobmincer.core.loot

import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack

fun interface LootFactory {
    fun generateLoot(): ObjectArrayList<ItemStack>

    companion object {
        val EMPTY = LootFactory { ObjectArrayList() }

        fun create(entity: LivingEntity, killedByPlayer: Boolean, lootingLevel: Int): LootFactory {
            return LootFactory { LootLookup.get(entity)!!.generateServerLoot(entity, killedByPlayer, lootingLevel) }
        }
    }
}
