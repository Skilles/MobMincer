package net.mobmincer.api.inventory

import net.minecraft.nbt.CompoundTag
import net.mobmincer.api.blockentity.BaseMachineBlockEntity

class MachineInventory<T : BaseMachineBlockEntity>(size: Int, val name: String, private val stackLimit: Int, val blockEntity: T, val access: InventoryAccess<T>) : BaseInventory(
    size
) {

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
