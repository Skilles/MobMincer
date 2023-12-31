package net.mobmincer.core.loot

import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.Mob
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.level.storage.loot.LootParams
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import net.mobmincer.FakePlayer

object LootFactoryCache {
    private val noLootCache = mutableMapOf<Pair<Class<out Mob>, Boolean>, Boolean>()

    fun getLootFactory(mob: Mob, killedByPlayer: Boolean = false, lootingLevel: Int = 0): LootFactory {
        if (mob.level().isClientSide) {
            return LootFactory.EMPTY
        }

        return mob.createLootFactory(killedByPlayer, lootingLevel)
    }

    fun hasLoot(mob: Mob, killedByPlayer: Boolean = false): Boolean {
        val key = Pair(mob.javaClass, killedByPlayer)
        return noLootCache.getOrPut(key) { mob.createLootFactory(killedByPlayer, 0) != LootFactory.EMPTY }
    }

    private fun Mob.createLootFactory(killedByPlayer: Boolean, lootingLevel: Int): LootFactory {
        val resourceLocation: ResourceLocation = this.lootTable
        val level = this.level() as ServerLevel
        val lootTable = level.server.lootData.getLootTable(resourceLocation)
        val fakePlayer = if (killedByPlayer) FakePlayer.create(level).get() else null
        val builder = LootParams.Builder(level)
            .withParameter(LootContextParams.THIS_ENTITY, this)
            .withParameter(LootContextParams.ORIGIN, this.position())
            .withParameter(
                LootContextParams.DAMAGE_SOURCE,
                fakePlayer?.let { level.damageSources().playerAttack(it) } ?: level.damageSources().generic()
            )
            .withOptionalParameter(LootContextParams.KILLER_ENTITY, fakePlayer)
            .withOptionalParameter(LootContextParams.DIRECT_KILLER_ENTITY, fakePlayer)
            .withOptionalParameter(LootContextParams.LAST_DAMAGE_PLAYER, fakePlayer)

        val lootParams = builder.create(LootContextParamSets.ENTITY)

        val hasLoot = lootTable.getRandomItems(lootParams, this.lootTableSeed).isNotEmpty()



        if (!hasLoot) {
            return LootFactory.EMPTY
        }

        return LootFactory {
            fakePlayer?.let {
                val tempSword = Items.DIAMOND_SWORD.defaultInstance
                if (lootingLevel > 0) {
                    tempSword.enchant(Enchantments.MOB_LOOTING, lootingLevel)
                }
                it.setItemInHand(InteractionHand.MAIN_HAND, tempSword)
            }
            val items = lootTable.getRandomItems(lootParams, lootTableSeed)
            fakePlayer?.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY)
            items
        }
    }
}
