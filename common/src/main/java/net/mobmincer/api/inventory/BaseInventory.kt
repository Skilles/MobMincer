package net.mobmincer.api.inventory

import net.minecraft.core.NonNullList
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.Container
import net.minecraft.world.ContainerHelper
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

abstract class BaseInventory(private val size: Int) : Container {

    private val items = NonNullList.withSize(size, ItemStack.EMPTY)

    fun toTag(): CompoundTag = ContainerHelper.saveAllItems(
        CompoundTag(),
        items
    )

    fun fromTag(tag: CompoundTag) {
        ContainerHelper.loadAllItems(
            tag,
            items
        )
    }

    override fun clearContent() = items.clear()

    override fun getContainerSize(): Int = size

    override fun isEmpty(): Boolean = items.all(ItemStack::isEmpty)

    override fun getItem(slot: Int): ItemStack = items[slot]

    override fun removeItem(slot: Int, amount: Int): ItemStack = ContainerHelper.removeItem(items, slot, amount).also {
        if (!it.isEmpty) {
            setChanged()
        }
    }

    override fun removeItemNoUpdate(slot: Int): ItemStack = ContainerHelper.takeItem(items, slot)

    override fun setItem(slot: Int, stack: ItemStack) {
        items[slot] = stack
        if (stack.count > maxStackSize) {
            stack.count = maxStackSize
        }

        setChanged()
    }

    override fun setChanged() = Unit

    override fun stillValid(player: Player): Boolean = true
}
