package net.mobmincer.api.inventory

import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.WorldlyContainer
import net.minecraft.world.item.ItemStack
import net.mobmincer.api.blockentity.BaseMachineBlockEntity

class MachineInventory<T : BaseMachineBlockEntity>(size: Int, val name: String, private val stackLimit: Int, val blockEntity: T, private val access: InventoryAccess<T>) :
    BaseInventory(
        size
    ),
    WorldlyContainer {

    var hasChanged: Boolean = false
        private set(value) {
            field = value
            if (value) {
                setChanged()
            }
        }

    constructor(size: Int, name: String, stackLimit: Int, blockEntity: T) : this(
        size,
        name,
        stackLimit,
        blockEntity,
        InventoryAccess { _, _, _, _, _ -> true }
    )

    override fun getSlotsForFace(side: Direction): IntArray {
        return IntArray(this.containerSize) { it }
    }

    override fun canPlaceItemThroughFace(index: Int, itemStack: ItemStack, direction: Direction?): Boolean = access.canHandleIO(
        index,
        itemStack,
        direction ?: Direction.UP,
        InventoryAccess.AccessDirection.INSERT,
        blockEntity
    )

    override fun canTakeItemThroughFace(index: Int, stack: ItemStack, direction: Direction): Boolean =
        access.canHandleIO(index, stack, direction, InventoryAccess.AccessDirection.EXTRACT, blockEntity)

    override fun getMaxStackSize(): Int = stackLimit

    override fun setChanged() {
        super.setChanged()
        blockEntity.setChanged()
    }

    fun readFromNBT(data: CompoundTag, key: String = "Items") {
        val tag = data.getCompound(key)
        fromTag(tag)
        hasChanged = true
    }

    fun writeToNBT(data: CompoundTag, key: String = "Items") {
        data.put(key, toTag())
    }
}
