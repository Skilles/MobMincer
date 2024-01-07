package net.mobmincer.core.loot

import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.level.storage.loot.LootParams
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import net.mobmincer.FakePlayer

class LootStackImpl(private val table: LootTable, entries: List<LootStack.LootStackEntry>) : LootStack {

    private val backingList: List<LootStack.LootStackEntry> = entries.sortedBy { it.playerOnly }

    private val playerOnlyIndex = entries.indexOfFirst { it.playerOnly }

    override val playerOnly: List<ItemStack> = if (playerOnlyIndex == -1) {
        emptyList()
    } else {
        backingList.subList(playerOnlyIndex, backingList.size)
    }.map { it.item }

    override val nonPlayerOnly: List<ItemStack> = if (playerOnlyIndex == -1) {
        backingList
    } else {
        backingList.subList(0, playerOnlyIndex)
    }.map { it.item }

    override fun generateServerLoot(entity: LivingEntity, includePlayerLoot: Boolean, lootingLevel: Int): ObjectArrayList<ItemStack> {
        val level = entity.level() as ServerLevel
        val fakePlayer = if (includePlayerLoot) FakePlayer.create(level).get() else null
        val builder = LootParams.Builder(level)
            .withParameter(LootContextParams.THIS_ENTITY, entity)
            .withParameter(LootContextParams.ORIGIN, entity.position())
            .withParameter(
                LootContextParams.DAMAGE_SOURCE,
                fakePlayer?.let { level.damageSources().playerAttack(it) } ?: level.damageSources().generic()
            )
            .withOptionalParameter(LootContextParams.KILLER_ENTITY, fakePlayer)
            .withOptionalParameter(LootContextParams.DIRECT_KILLER_ENTITY, fakePlayer)
            .withOptionalParameter(LootContextParams.LAST_DAMAGE_PLAYER, fakePlayer)

        val lootParams = builder.create(LootContextParamSets.ENTITY)

        fakePlayer?.let {
            val tempSword = Items.DIAMOND_SWORD.defaultInstance
            if (lootingLevel > 0) {
                tempSword.enchant(Enchantments.MOB_LOOTING, lootingLevel)
            }
            it.setItemInHand(InteractionHand.MAIN_HAND, tempSword)
        }
        val items = table.getRandomItems(lootParams, entity.lootTableSeed)
        fakePlayer?.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY)
        return items
    }

    companion object {
        val EMPTY = LootStackImpl(LootTable.EMPTY)
    }

    private constructor(table: LootTable) : this(table, emptyList())

    init {
        require(entries.isNotEmpty() || this === EMPTY || EMPTY == null)
    }

    override val size: Int
        get() = backingList.size

    override fun contains(element: LootStack.LootStackEntry): Boolean {
        return backingList.contains(element)
    }

    override fun containsAll(elements: Collection<LootStack.LootStackEntry>): Boolean {
        return backingList.containsAll(elements)
    }

    override fun get(index: Int): LootStack.LootStackEntry {
        return backingList[index]
    }

    override fun indexOf(element: LootStack.LootStackEntry): Int {
        return backingList.indexOf(element)
    }

    override fun isEmpty(): Boolean {
        return this === EMPTY
    }

    override fun iterator(): Iterator<LootStack.LootStackEntry> {
        return backingList.iterator()
    }

    override fun lastIndexOf(element: LootStack.LootStackEntry): Int {
        return backingList.lastIndexOf(element)
    }

    override fun listIterator(): ListIterator<LootStack.LootStackEntry> {
        return backingList.listIterator()
    }

    override fun listIterator(index: Int): ListIterator<LootStack.LootStackEntry> {
        return backingList.listIterator(index)
    }

    override fun subList(fromIndex: Int, toIndex: Int): List<LootStack.LootStackEntry> {
        return backingList.subList(fromIndex, toIndex)
    }
}
